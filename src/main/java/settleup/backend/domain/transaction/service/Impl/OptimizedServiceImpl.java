package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;

import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.*;
import settleup.backend.domain.transaction.model.TransactionalEntity;
import settleup.backend.domain.transaction.repository.OptimizedTransactionDetailsRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Transactional
public class OptimizedServiceImpl implements OptimizedService {
    private GroupUserRepository groupUserRepo;
    private RequireTransactionRepository requireTransactionRepo;
    private OptimizedTransactionRepository optimizedTransactionRepo;
    private OptimizedTransactionDetailsRepository optimizedTransactionDetailsRepo;
    private UserRepository userRepo;
    private UUID_Helper uuidHelper;
    private final TransactionInheritanceService transactionInheritanceService;

    private static final Logger log = LoggerFactory.getLogger(OptimizedServiceImpl.class);

    /**
     * optimizationOfp2p
     *
     * @throws CustomException
     * @process 1. optimizationOfp2p
     * 2. createCombinationList
     * 3. optimizationTargetList = targetDto.group 의 모든 requireTransaction 중에 clear 상태가 아닌것 불러오기
     * 4. if sender , recipient 가 combinationList와 일치하면 그 리스트들의 불러와서 ,
     * combinationList [0,1]  totalAmount.build
     * case1) sender 가 (0) 일때 amount +
     * case2) sender 가 (1) 일때 amount -
     * result1) if (totalAmount >0) {sender=(0),recipient(1) , totalAmount 는 그대로
     * }else{sender=(1), recipient=(0), |totalAmount| }
     */
    @Override
    public TransactionP2PResultDto optimizationOfp2p(UserGroupDto groupDto) throws CustomException {
        Boolean isUserType = groupDto.getIsUserType();
        optimizedTransactionRepo.updateOptimizationStatusByGroup(groupDto.getGroup(), Status.PREVIOUS);
        List<List<Long>> nodeList = createCombinationList(groupDto.getGroup());
        System.out.println("heyNode:" + nodeList);
        return optimizationTargetList(groupDto.getGroup(), nodeList);
    }

    private TransactionP2PResultDto optimizationTargetList(GroupTypeEntity group, List<List<Long>> nodeList) {
        TransactionP2PResultDto resultDto = new TransactionP2PResultDto();
        resultDto.setNodeList(nodeList);
        resultDto.setGroup(group);

        List<Long> savedOptimizedTransactionIds = new ArrayList<>();
        List<RequiresTransactionEntity> targetGroupList = requireTransactionRepo.findActiveTransactionsByGroup(group.getId());

        System.out.println("here's List size:" + targetGroupList.size());
        System.out.println("Total node pairs to process: " + nodeList.size());

        for (List<Long> node : nodeList) {
            Long senderId = node.get(0);
            Long recipientId = node.get(1);

            System.out.println("Processing pair - Sender ID: " + senderId + ", Recipient ID: " + recipientId);

            List<RequiresTransactionEntity> filteredTransactions = new ArrayList<>();
            for (RequiresTransactionEntity transaction : targetGroupList) {
                if ((transaction.getSenderUser().getId().equals(senderId) && transaction.getRecipientUser().getId().equals(recipientId))
                        || (transaction.getSenderUser().getId().equals(recipientId) && transaction.getRecipientUser().getId().equals(senderId))) {
                    filteredTransactions.add(transaction);
                }
            }


            System.out.println("Filtered transactions count for this pair: " + filteredTransactions.size());
            if (!filteredTransactions.isEmpty()) {
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (RequiresTransactionEntity transaction : filteredTransactions) {
                    if (transaction.getSenderUser().getId().equals(senderId)) {
                        totalAmount = totalAmount.add(transaction.getTransactionAmount());
                    } else if (transaction.getSenderUser().getId().equals(recipientId)) {
                        totalAmount = totalAmount.subtract(transaction.getTransactionAmount());
                    }
                }

                IntermediateCalcDto intermediateCalcDto = new IntermediateCalcDto();
                intermediateCalcDto.setGroup(group);
                intermediateCalcDto.setTransactionAmount(totalAmount.abs());
                intermediateCalcDto.setDuringOptimizationUsed(filteredTransactions);

                if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(senderId).orElse(null));
                    intermediateCalcDto.setRecipientUser(userRepo.findById(recipientId).orElse(null));
                } else if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    intermediateCalcDto.setSenderUser(userRepo.findById(recipientId).orElse(null));
                    intermediateCalcDto.setRecipientUser(userRepo.findById(senderId).orElse(null));
                }

                if (totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                    OptimizedTransactionEntity optimizedTransaction = new OptimizedTransactionEntity();
                    optimizedTransaction.setTransactionUUID(uuidHelper.UUIDForOptimizedTransaction());
                    optimizedTransaction.setGroup((GroupEntity) intermediateCalcDto.getGroup());
                    optimizedTransaction.setSenderUser((UserEntity) intermediateCalcDto.getSenderUser());
                    optimizedTransaction.setRecipientUser((UserEntity) intermediateCalcDto.getRecipientUser());
                    optimizedTransaction.setTransactionAmount(intermediateCalcDto.getTransactionAmount());
                    optimizedTransaction.setHasBeenSent(false);
                    optimizedTransaction.setHasBeenChecked(false);
                    optimizedTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
                    optimizedTransaction.setOptimizationStatus(Status.CURRENT);
                    optimizedTransaction.setCreatedAt(LocalDateTime.now());
                    OptimizedTransactionEntity savedOptimizedTransaction =
                            optimizedTransactionRepo.save(optimizedTransaction);
                    savedOptimizedTransactionIds.add(savedOptimizedTransaction.getId());
                    resultDto.setOptimiziationByPeerToPeerList(savedOptimizedTransactionIds);

                    for (RequiresTransactionEntity transaction : intermediateCalcDto.getDuringOptimizationUsed()) {
                        OptimizedTransactionDetailsEntity details = new OptimizedTransactionDetailsEntity();
                        details.setTransactionDetailUUID(uuidHelper.UUIDForOptimizedTransactionsDetail());
                        details.setOptimizedTransaction(optimizedTransaction);
                        details.setRequiresTransaction(transaction);
                        optimizedTransactionDetailsRepo.save(details);
                    }
                } else {
                    LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
                    for (RequiresTransactionEntity transactionForClear : filteredTransactions) {
                        transactionForClear.setRequiredReflection(Status.INHERITED_CLEAR);
                        requireTransactionRepo.save(transactionForClear);
                        requireTransactionRepo.updateClearStatusTimestampById(transactionForClear.getId(), newClearStatusTimestamp);
                    }
                }
            }
        }
        return resultDto;
    }


    private List<List<Long>> createCombinationList(GroupTypeEntity group) throws CustomException {
        // 그룹 id 로 그룹 사용자 리스트를 조회
        List<GroupUserEntity> groupUserList = groupUserRepo.findByGroup_Id(group.getId());
        if (groupUserList.isEmpty()) {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        // GroupUserEntity 목록에서 UserEntity 목록으로 반환
        List<UserTypeEntity> userList = groupUserList.stream()
                .map(GroupUserEntity::getUser)
                .collect(Collectors.toList());

        // Further processing
        List<Long> userFKList = userList.stream().map(UserTypeEntity::getId).collect(Collectors.toList());

        // Make combinationList
        List<List<Long>> combinationList = new ArrayList<>();
        for (int i = 0; i < userFKList.size(); i++) {
            for (int j = i + 1; j < userFKList.size(); j++) {
                combinationList.add(List.of(userFKList.get(i), userFKList.get(j)));
            }
        }
        return combinationList;
    }


    @Override
    @Transactional
    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, GroupTypeEntity existingGroup) throws CustomException {
        log.info("Processing transaction with Transaction ID: {}", request.getTransactionId());
        OptimizedTransactionEntity transactionEntity = optimizedTransactionRepo.findByTransactionUUID(request.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        log.info("Found transaction entity for UUID: {}", transactionEntity.getTransactionUUID());

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            log.error("Transaction group ID {} does not match existing group ID {}", transactionEntity.getGroup().getId(), existingGroup.getId());
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        List<RequiresTransactionEntity> requiresTransactions = optimizedTransactionDetailsRepo.findRequiresTransactionsByOptimizedTransactionId(transactionEntity.getId());

        log.info("Found {} RequiresTransactionEntities for OptimizedTransaction ID {}", requiresTransactions.size(), transactionEntity.getId());

        for (RequiresTransactionEntity requiresTransaction : requiresTransactions) {
            log.info("Processing RequiresTransaction ID {}", requiresTransaction.getId());
            transactionInheritanceService.clearInheritanceStatusFromOptimizedToRequired(requiresTransaction.getId());
        }

        return transactionEntity;
    }


}
