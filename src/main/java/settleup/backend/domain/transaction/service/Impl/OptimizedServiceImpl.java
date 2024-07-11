package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.group.entity.AbstractGroupEntity;

import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.*;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.repository.OptimizedTransactionDetailsRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;
import settleup.backend.domain.user.entity.AbstractUserEntity;

import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.repository.UserBaseRepository;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Selector.UserRepoSelector;
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
    private UUID_Helper uuidHelper;
    private UserRepoSelector selector;
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
            log.debug("Starting optimizationOfp2p for groupDto: {}", groupDto);
            Boolean isUserType = groupDto.getIsUserType();
            optimizedTransactionRepo.updateOptimizationStatusByGroup(groupDto.getGroup(), Status.PREVIOUS);
            List<List<Long>> nodeList = createCombinationList(groupDto.getGroup());
            log.debug("Node list created: {}", nodeList);
            log.info("optimizationOfp2p OK");
            return optimizationTargetList(groupDto.getGroup(), nodeList,isUserType);
        }

        private TransactionP2PResultDto optimizationTargetList(AbstractGroupEntity group, List<List<Long>> nodeList ,Boolean isUserType) {
            log.debug("Starting optimizationTargetList for group: {} with nodeList: {}", group, nodeList);
            TransactionP2PResultDto resultDto = new TransactionP2PResultDto();
            resultDto.setNodeList(nodeList);
            resultDto.setGroup(group);

            List<Long> savedOptimizedTransactionIds = new ArrayList<>();

            List<RequiresTransactionEntity> targetGroupList = requireTransactionRepo.findActiveTransactionsByGroup(group.getId());

            log.debug("Target group list size: {}", targetGroupList.size());
            log.debug("Total node pairs to process: {}", nodeList.size());

            for (List<Long> node : nodeList) {
                Long senderId = node.get(0);
                Long recipientId = node.get(1);

                log.debug("Processing pair - Sender ID: {}, Recipient ID: {}", senderId, recipientId);

                List<RequiresTransactionEntity> filteredTransactions = new ArrayList<>();
                for (RequiresTransactionEntity transaction : targetGroupList) {
                    if ((transaction.getSenderUser().getId().equals(senderId) && transaction.getRecipientUser().getId().equals(recipientId))
                            || (transaction.getSenderUser().getId().equals(recipientId) && transaction.getRecipientUser().getId().equals(senderId))) {
                        filteredTransactions.add(transaction);
                    }
                }

                log.debug("Filtered transactions count for this pair: {}", filteredTransactions.size());
                if (!filteredTransactions.isEmpty()) {
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    for (RequiresTransactionEntity transaction : filteredTransactions) {
                        log.debug("Processing transaction ID: {}", transaction.getId());
                        if (transaction.getSenderUser().getId().equals(senderId)) {
                            totalAmount = totalAmount.add(transaction.getTransactionAmount());
                            log.debug("Added amount: {} to totalAmount for senderId: {}", transaction.getTransactionAmount(), senderId);
                        } else if (transaction.getSenderUser().getId().equals(recipientId)) {
                            totalAmount = totalAmount.subtract(transaction.getTransactionAmount());
                            log.debug("Subtracted amount: {} from totalAmount for recipientId: {}", transaction.getTransactionAmount(), recipientId);
                        }
                    }

                    IntermediateCalcDto intermediateCalcDto = new IntermediateCalcDto();
                    intermediateCalcDto.setGroup(group);
                    intermediateCalcDto.setTransactionAmount(totalAmount.abs());
                    intermediateCalcDto.setDuringOptimizationUsed(filteredTransactions);

                    UserBaseRepository<? extends AbstractUserEntity> userRepo =selector.getUserRepository(isUserType);


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
                        optimizedTransaction.setGroup( intermediateCalcDto.getGroup());
                        optimizedTransaction.setSenderUser( intermediateCalcDto.getSenderUser());
                        optimizedTransaction.setRecipientUser( intermediateCalcDto.getRecipientUser());
                        optimizedTransaction.setTransactionAmount(intermediateCalcDto.getTransactionAmount());
                        optimizedTransaction.setHasBeenSent(false);
                        optimizedTransaction.setHasBeenChecked(false);
                        optimizedTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
                        optimizedTransaction.setOptimizationStatus(Status.CURRENT);
                        optimizedTransaction.setCreatedAt(LocalDateTime.now());
                        Status userType = isUserType != null && isUserType ? Status.REGULAR : Status.DEMO;
                        optimizedTransaction.setUserType(userType);
                        OptimizedTransactionEntity savedOptimizedTransaction = optimizedTransactionRepo.save(optimizedTransaction);
                        log.debug("Saved optimized transaction with ID: {}", savedOptimizedTransaction.getId());
                        savedOptimizedTransactionIds.add(savedOptimizedTransaction.getId());
                        resultDto.setOptimiziationByPeerToPeerList(savedOptimizedTransactionIds);
                        resultDto.setIsUserType(isUserType);

                        for (RequiresTransactionEntity transaction : intermediateCalcDto.getDuringOptimizationUsed()) {
                            OptimizedTransactionDetailsEntity details = new OptimizedTransactionDetailsEntity();
                            details.setTransactionDetailUUID(uuidHelper.UUIDForOptimizedTransactionsDetail());
                            details.setOptimizedTransaction(optimizedTransaction);
                            details.setRequiresTransaction(transaction);
                            optimizedTransactionDetailsRepo.save(details);
                            log.debug("Saved optimized transaction detail for transaction ID: {}", transaction.getId());
                        }
                    } else {
                        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
                        for (RequiresTransactionEntity transactionForClear : filteredTransactions) {
                            transactionForClear.setRequiredReflection(Status.INHERITED_CLEAR);
                            requireTransactionRepo.save(transactionForClear);
                            requireTransactionRepo.updateClearStatusTimestampById(transactionForClear.getId(), newClearStatusTimestamp);
                            log.debug("Cleared transaction with ID: {}", transactionForClear.getId());
                        }
                    }
                }
            }
            log.info("optimizationTargetList OK");
            return resultDto;
        }

        private List<List<Long>> createCombinationList(AbstractGroupEntity group) throws CustomException {
            log.debug("Creating combination list for group ID: {}", group.getId());
            Boolean isRegularUser = null;
            if (group.getGroupType() == Status.REGULAR) {
                isRegularUser = true;
            } else if (group.getGroupType() == Status.DEMO) {
                isRegularUser = false;
            }

            if (isRegularUser == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            List<AbstractGroupUserEntity> groupUserList = selector.getGroupUserRepository(isRegularUser).findByGroup_Id(group.getId());
            if (groupUserList.isEmpty()) {
                log.error("No group users found for group ID: {}", group.getId());
                throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
            }

            List<AbstractUserEntity> userList = groupUserList.stream()
                    .map(AbstractGroupUserEntity::getUser)
                    .collect(Collectors.toList());

            List<Long> userFKList = userList.stream().map(AbstractUserEntity::getId).collect(Collectors.toList());
            log.debug("User FK List: {}", userFKList);

            List<List<Long>> combinationList = new ArrayList<>();
            for (int i = 0; i < userFKList.size(); i++) {
                for (int j = i + 1; j < userFKList.size(); j++) {
                    combinationList.add(List.of(userFKList.get(i), userFKList.get(j)));
                }
            }
            log.debug("Combination List: {}", combinationList);
            return combinationList;
        }

    @Override
    @Transactional
    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, AbstractGroupEntity existingGroup) throws CustomException {
        log.info("Processing transaction with Transaction ID: {}", request.getTransactionId());
        OptimizedTransactionEntity transactionEntity = optimizedTransactionRepo.findByTransactionUUID(request.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        log.info("Found transaction entity for UUID: {}", transactionEntity.getTransactionUUID());

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            log.error("Transaction group ID {} does not match existing group ID {}", transactionEntity.getGroup().getId(), existingGroup.getId());
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        // optimizedTransactionId를 사용하여 상속 상태를 클리어
        transactionInheritanceService.clearInheritanceStatusFromOptimizedToRequired(transactionEntity.getId());

        return transactionEntity;
    }
}

