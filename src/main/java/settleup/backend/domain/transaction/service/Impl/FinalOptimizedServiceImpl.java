package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.CombinedListDto;
import settleup.backend.domain.transaction.entity.dto.IntermediateCalcDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.FinalOptimizedService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FinalOptimizedServiceImpl implements FinalOptimizedService {
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedTransactionDetailRepo;
    private final FinalOptimizedTransactionRepository mergeTransactionRepo;
    private final FinalOptimizedTransactionDetailRepository mergeTransactionDetailsRepo;
    private final OptimizedTransactionRepository p2pRepo;
    private final UserRepository userRepo;
    private final OptimizedTransactionDetailsRepository p2pDetailRepo;
    private final UUID_Helper uuidHelper;

    @Override
    public void lastMergeTransaction(TransactionP2PResultDto resultDto) {
        GroupEntity group =  p2pRepo.findGroupByTransactionId(resultDto.getP2pList().get(0));
        List<CombinedListDto> targetList=getCombinedList(group);
        mergeTransaction(targetList,resultDto.getNodeList(),group);
    }

    private void mergeTransaction(List<CombinedListDto> targetList, List<List<Long>> nodeList,GroupEntity group) {
        mergeTransactionRepo.updateIsUsedStatusByGroup(group,Status.USED);
        List<Long> saveFinalOptimizedIds = new ArrayList<>();

        for (List<Long> node : nodeList) {
            Long senderId = node.get(0);
            Long recipientId = node.get(1);

            List<CombinedListDto> filteredTransactionsFinal = new ArrayList<>();
            for (CombinedListDto combinedTransaction : targetList) {
                if ((combinedTransaction.getSenderUser().getId().equals(senderId) && combinedTransaction.getRecipientUser().getId().equals(recipientId))
                        || (combinedTransaction.getSenderUser().getId().equals(recipientId) && combinedTransaction.getRecipientUser().getId().equals(senderId))) {
                    filteredTransactionsFinal.add(combinedTransaction);
                }
            }
            if (!filteredTransactionsFinal.isEmpty()) {
                double totalFinalAmount = 0;
                for (CombinedListDto transaction : filteredTransactionsFinal) {
                    if (transaction.getSenderUser().getId().equals(senderId)) {
                        totalFinalAmount += transaction.getOptimizedAmount();
                    } else if (transaction.getSenderUser().getId().equals(recipientId)) {
                        totalFinalAmount -= transaction.getOptimizedAmount();
                    }
                }

                IntermediateCalcDto intermediateCalcDto = new IntermediateCalcDto();
                intermediateCalcDto.setGroup(group);
                intermediateCalcDto.setTransactionAmount(Math.abs(totalFinalAmount));
                intermediateCalcDto.setDuringFinalOptimizationUsed(filteredTransactionsFinal);

                if (totalFinalAmount > 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(senderId).get());
                    intermediateCalcDto.setRecipientUser(userRepo.findById(recipientId).get());
                } else if (totalFinalAmount < 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(recipientId).get());
                    intermediateCalcDto.setRecipientUser(userRepo.findById(senderId).get());
                }

                if (totalFinalAmount != 0) {
                    FinalOptimizedTransactionEntity finalOptimizedTransaction = new FinalOptimizedTransactionEntity();
                    finalOptimizedTransaction.setFinalOptimizedTransactionUUID(uuidHelper.UUIDForFinalOptimized());
                    finalOptimizedTransaction.setGroup(intermediateCalcDto.getGroup());
                    finalOptimizedTransaction.setSenderUser(intermediateCalcDto.getSenderUser());
                    finalOptimizedTransaction.setRecipientUser(intermediateCalcDto.getRecipientUser());
                    finalOptimizedTransaction.setOptimizedAmount(intermediateCalcDto.getTransactionAmount());
                    finalOptimizedTransaction.setIsCleared(Status.PENDING);
                    finalOptimizedTransaction.setIsUsed(Status.NOT_USED);
                    finalOptimizedTransaction.setCreatedAt(LocalDateTime.now());
                    FinalOptimizedTransactionEntity saveFinalOptimizedTransaction =
                            mergeTransactionRepo.save(finalOptimizedTransaction);
                    saveFinalOptimizedIds.add(saveFinalOptimizedTransaction.getId());

                    for (CombinedListDto transaction : intermediateCalcDto.getDuringFinalOptimizationUsed()) {
                        FinalOptimizedTransactionDetailEntity finalOptimizedTransactionDetail = new FinalOptimizedTransactionDetailEntity();
                        finalOptimizedTransactionDetail.setFinalOptimizedTransactionDetailUUID(uuidHelper.UUIDForFinalOptimizedDetail());
                        finalOptimizedTransactionDetail.setFinalOptimizedTransaction(finalOptimizedTransaction);
                        finalOptimizedTransactionDetail.setUsedOptimizedTransaction(transaction.getOptimizedUUID());
                        mergeTransactionDetailsRepo.save(finalOptimizedTransactionDetail);
                    }
                } else {
                    for (CombinedListDto transactionForClear : filteredTransactionsFinal) {
                        String uuid = transactionForClear.getOptimizedUUID();
                        if (uuid.startsWith("OPT")) {

                            OptimizedTransactionEntity optimizedTransaction = p2pRepo.findByOptimizedTransactionUUID(uuid);
                            if (optimizedTransaction != null) {
                                optimizedTransaction.setIsCleared(Status.CLEAR);
                                p2pRepo.save(optimizedTransaction);
                            }
                        } else if (uuid.startsWith("GPT")) {
                            GroupOptimizedTransactionEntity groupOptimizedTransaction = groupOptimizedTransactionRepo.findByGroupOptimizedTransactionUUID(uuid);
                            if (groupOptimizedTransaction != null) {
                                groupOptimizedTransaction.setIsCleared(Status.CLEAR);
                                groupOptimizedTransactionRepo.save(groupOptimizedTransaction);
                            }

                        }
                    }
                }
            }
        }
    }
    private List<CombinedListDto> getCombinedList(GroupEntity group) {
        List<GroupOptimizedTransactionEntity> optimizedList =
                groupOptimizedTransactionRepo.findByGroupAndIsUsed(group, Status.NOT_USED);
        List<OptimizedTransactionEntity> optimizedP2PList = new ArrayList<>();

        List<CombinedListDto> combinedDtoList = new ArrayList<>();

        // GroupOptimizedTransactionEntity 목록을 순회하며 CombinedListDto 리스트 생성
        for (GroupOptimizedTransactionEntity optimizedTransaction : optimizedList) {
            CombinedListDto dto = new CombinedListDto(
                    optimizedTransaction.getGroupOptimizedTransactionUUID(),
                    optimizedTransaction.getSenderUser(),
                    optimizedTransaction.getRecipientUser(),
                    optimizedTransaction.getOptimizedAmount());
            combinedDtoList.add(dto);

            // 현재 최적화된 트랜잭션에 해당하는 사용가능한 P2P 트랜잭션 조회
            List<OptimizedTransactionEntity> availableTransactions =
                    p2pRepo.findAvailableOptimizedTransactions(optimizedTransaction.getGroup(), optimizedTransaction.getId());
            optimizedP2PList.addAll(availableTransactions);
        }

        // OptimizedTransactionEntity 목록을 순회하며 CombinedListDto 리스트에 추가
        for (OptimizedTransactionEntity optimizedP2P : optimizedP2PList) {
            CombinedListDto dto = new CombinedListDto(
                    optimizedP2P.getOptimizedTransactionUUID(),
                    optimizedP2P.getSenderUser(),
                    optimizedP2P.getRecipientUser(),
                    optimizedP2P.getTransactionAmount());
            combinedDtoList.add(dto);
        }

        return combinedDtoList;
    }

}
