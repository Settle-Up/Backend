package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.*;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.UltimateOptimizedService;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class UltimateOptimizedServiceImpl implements UltimateOptimizedService {
    private final GroupOptimizedTransactionRepository groupOptimizedRepo;
    private final UltimateOptimizedTransactionRepository ultimateRepo;
    private final UltimateOptimizedTransactionDetailRepository ultimateDetailRepo;
    private final OptimizedTransactionRepository optimizedRepo;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;
    private final TransactionInheritanceService inheritanceService;

    @Override
    public void ultimateOptimizedTransaction(TransactionP2PResultDto resultDto) {
        List<TransactionalEntity> combinedListForUltimateProcessing = getCombinedList(resultDto);
        ultimateTransaction(combinedListForUltimateProcessing, resultDto);
    }

    private List<TransactionalEntity> getCombinedList(TransactionP2PResultDto resultDto) {
        List<TransactionalEntity> groupOptimizedCurrentList =
                groupOptimizedRepo.findByGroupAndOptimizationStatus(resultDto.getGroup(), Status.CURRENT);

        List<TransactionalEntity> p2pCurrentAndReflectStatusList =
                optimizedRepo.findTransactionsByGroupAndStatus(resultDto.getGroup(), Status.CURRENT, Status.REQUIRE_REFLECT);

        List<TransactionalEntity> combinedList = new ArrayList<>();
        combinedList.addAll(groupOptimizedCurrentList);
        combinedList.addAll(p2pCurrentAndReflectStatusList);

        return combinedList;
    }

    private void ultimateTransaction(List<TransactionalEntity> combinedListForUltimateProcessing, TransactionP2PResultDto result) {
        List<List<Long>> nodeList = result.getNodeList();
        GroupEntity group = result.getGroup();

        ultimateRepo.updateOptimizationStatusByGroup(result.getGroup(), Status.PREVIOUS);

        List<Long> listForSaveUltimateIds = new ArrayList<>();

        for (List<Long> node : nodeList) {
            Long senderId = node.get(0);
            Long recipientId = node.get(1);

            List<TransactionalEntity> filteredTransactionsUltimate = new ArrayList<>();

            for (TransactionalEntity combinedTransaction : combinedListForUltimateProcessing) {
                if ((combinedTransaction.getSenderUser().getId().equals(senderId) && combinedTransaction.getRecipientUser().getId().equals(recipientId))
                        || (combinedTransaction.getSenderUser().getId().equals(recipientId) && combinedTransaction.getRecipientUser().getId().equals(senderId))) {
                    filteredTransactionsUltimate.add(combinedTransaction);
                }
            }

            if (!filteredTransactionsUltimate.isEmpty()) {
                BigDecimal totalFinalAmount = BigDecimal.ZERO;
                for (TransactionalEntity transaction : filteredTransactionsUltimate) {
                    if (transaction.getSenderUser().getId().equals(senderId)) {
                        totalFinalAmount = totalFinalAmount.add(transaction.getTransactionAmount());
                    } else if (transaction.getSenderUser().getId().equals(recipientId)) {
                        totalFinalAmount = totalFinalAmount.subtract(transaction.getTransactionAmount());
                    }
                }

                IntermediateCalcDto intermediateCalcDto = new IntermediateCalcDto();
                intermediateCalcDto.setGroup(group);
                intermediateCalcDto.setTransactionAmount(totalFinalAmount.abs());
                intermediateCalcDto.setDuringFinalOptimizationUsed(filteredTransactionsUltimate);

                if (totalFinalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(senderId).get());
                    intermediateCalcDto.setRecipientUser(userRepo.findById(recipientId).get());
                } else if (totalFinalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(recipientId).get());
                    intermediateCalcDto.setRecipientUser(userRepo.findById(senderId).get());
                }

                if (totalFinalAmount.compareTo(BigDecimal.ZERO) != 0) {
                    UltimateOptimizedTransactionEntity ultimateOptimizedTransaction = new UltimateOptimizedTransactionEntity();
                    ultimateOptimizedTransaction.setTransactionUUID(uuidHelper.UUIDForFinalOptimized());
                    ultimateOptimizedTransaction.setGroup(intermediateCalcDto.getGroup());
                    ultimateOptimizedTransaction.setSenderUser(intermediateCalcDto.getSenderUser());
                    ultimateOptimizedTransaction.setRecipientUser(intermediateCalcDto.getRecipientUser());
                    ultimateOptimizedTransaction.setTransactionAmount(intermediateCalcDto.getTransactionAmount());
                    ultimateOptimizedTransaction.setHasBeenSent(false);
                    ultimateOptimizedTransaction.setHasBeenChecked(false);
                    ultimateOptimizedTransaction.setOptimizationStatus(Status.CURRENT);
                    ultimateOptimizedTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
                    ultimateOptimizedTransaction.setCreatedAt(LocalDateTime.now());
                    UltimateOptimizedTransactionEntity saveFinalOptimizedTransaction =
                            ultimateRepo.save(ultimateOptimizedTransaction);
                    listForSaveUltimateIds.add(saveFinalOptimizedTransaction.getId());

                    for (TransactionalEntity transaction : intermediateCalcDto.getDuringFinalOptimizationUsed()) {
                        UltimateOptimizedTransactionDetailEntity finalOptimizedTransactionDetail = new UltimateOptimizedTransactionDetailEntity();
                        finalOptimizedTransactionDetail.setTransactionDetailUUID(uuidHelper.UUIDForFinalOptimizedDetail());
                        finalOptimizedTransactionDetail.setUltimateOptimizedTransaction(ultimateOptimizedTransaction);
                        finalOptimizedTransactionDetail.setUsedOptimizedTransaction(transaction.getTransactionUUID());
                        ultimateDetailRepo.save(finalOptimizedTransactionDetail);

                        if (transaction.getTransactionUUID().startsWith("OPT")) {
                            optimizedRepo.updateRequiredReflectionByTransactionUUID(transaction.getTransactionUUID(), Status.INHERITED);
                        } else if (transaction.getTransactionUUID().startsWith("GPT")) {
                            groupOptimizedRepo.updateRequiredReflectionByTransactionUUID(transaction.getTransactionUUID(), Status.INHERITED);
                        }
                    }
                } else {
                    filteredTransactionsUltimate.forEach(transactionForClear -> {
                        String uuid = transactionForClear.getTransactionUUID();
                        if (uuid.startsWith("OPT")) {
                            Optional<OptimizedTransactionEntity> optimizedTransactionOpt = optimizedRepo.findByTransactionUUID(uuid);
                            optimizedTransactionOpt.ifPresent(optimizedTransaction -> {
                                optimizedRepo.updateRequiredReflectionByTransactionUUID(uuid, Status.INHERITED_CLEAR);
                            });
                        } else if (uuid.startsWith("GPT")) {
                            Optional<GroupOptimizedTransactionEntity> groupOptimizedTransactionOpt = groupOptimizedRepo.findByTransactionUUID(uuid);
                            groupOptimizedTransactionOpt.ifPresent(groupOptimizedTransaction -> {
                                groupOptimizedRepo.updateRequiredReflectionByTransactionUUID(uuid, Status.INHERITED_CLEAR);
                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    @Transactional
    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, GroupEntity existingGroup) throws CustomException {
        UltimateOptimizedTransactionEntity transactionEntity = ultimateRepo.findByTransactionUUID(request.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        List<String> requireInheritanceSelectedList = ultimateDetailRepo.findUsedOptimizedTransactionUuidsByUltimateTransactionId(
                ultimateRepo.findByTransactionUUID(transactionEntity.getTransactionUUID()).get().getId());

        for (String requireSelectServiceFromUUID : requireInheritanceSelectedList) {
            selectServiceUpdateReflectionByInheritance(requireSelectServiceFromUUID);
        }

        return transactionEntity;
    }

    private void selectServiceUpdateReflectionByInheritance(String transactionId) {
        if (transactionId.startsWith("GPT")) {
            Optional<GroupOptimizedTransactionEntity> groupTransaction = groupOptimizedRepo.findByTransactionUUID(transactionId);
            if (!groupTransaction.isPresent()) {
                throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
            }
            inheritanceService.clearInheritanceStatusFromUltimateToGroup(groupTransaction);
        } else if (transactionId.startsWith("OPT")) {
            Optional<OptimizedTransactionEntity> optimizedTransaction = optimizedRepo.findByTransactionUUID(transactionId);
            if (!optimizedTransaction.isPresent()) {
                throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
            }
            inheritanceService.clearInheritanceStatusFromUltimateToOptimized(optimizedTransaction);
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }
    }
}
