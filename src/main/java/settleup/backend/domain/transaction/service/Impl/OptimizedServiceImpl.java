package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.P2PDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PCalculationResultDto;
import settleup.backend.domain.transaction.repository.OptimizedTransactionDetailsRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Transactional
public class OptimizedServiceImpl implements OptimizedService {
    private GroupUserRepository groupUserRepo;
    private RequireTransactionRepository transactionRepo;
    private OptimizedTransactionRepository optimizedTransactionRepo;
    private OptimizedTransactionDetailsRepository optimizedTransactionDetailsRepo;
    private RequireTransactionRepository requireTransactionRepo;
    private UserRepository userRepo;
    private UUID_Helper uuidHelper;

    /**
     * optimizationOfp2p
     *
     * @param targetDto (receipt , group, allocationType , payerUser)
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
    public List<Long> optimizationOfp2p(TransactionDto targetDto) throws CustomException {
        optimizedTransactionRepo.updateIsUsedStatusByGroup(targetDto.getGroup(), Status.USED);
        List<List<Long>> nodeList = createCombinationList(targetDto.getGroup());
        System.out.println("heyNode:" + nodeList);
        return optimizationTargetList(targetDto.getGroup(), nodeList);
    }

    private List<Long> optimizationTargetList(GroupEntity group, List<List<Long>> nodeList) {
        List<RequiresTransactionEntity> targetGroupList = requireTransactionRepo.findByGroupIdAndStatusNotClear(group.getId());
        List<Long> savedOptimizedTransactionIds = new ArrayList<>();
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

            // 거래 리스트가 필터링된 후, 해당 거래들에 대해 정보를 출력합니다.
            System.out.println("Filtered transactions count for this pair: " + filteredTransactions.size());
            if (!filteredTransactions.isEmpty()) {
                double totalAmount = 0;
                for (RequiresTransactionEntity transaction : filteredTransactions) {
                    if (transaction.getSenderUser().getId().equals(senderId)) {
                        totalAmount += transaction.getTransactionAmount();
                    } else if (transaction.getSenderUser().getId().equals(recipientId)) {
                        totalAmount -= transaction.getTransactionAmount();
                    }
                }

                // P2PDto 객체 생성 및 설정
                P2PDto p2PDto = new P2PDto();
                p2PDto.setGroup(group);
                p2PDto.setTransactionAmount(Math.abs(totalAmount));
                p2PDto.setDuringOptimizationUsed(filteredTransactions);

                // Sender와 Recipient 설정
                if (totalAmount > 0) {
                    p2PDto.setSenderUser(userRepo.findById(senderId).get());
                    p2PDto.setRecipientUser(userRepo.findById(recipientId).get());
                } else if (totalAmount < 0) {
                    p2PDto.setSenderUser(userRepo.findById(recipientId).get());
                    p2PDto.setRecipientUser(userRepo.findById(senderId).get());
                }

                // totalAmount가 0이 아닌 경우, OptimizedTransactionEntity 및 OptimizedTransactionDetailsEntity 저장
                if (totalAmount != 0) {
                    OptimizedTransactionEntity optimizedTransaction = new OptimizedTransactionEntity();
                    optimizedTransaction.setOptimizedTransactionUUID(uuidHelper.UUIDForOptimizedTransaction());
                    optimizedTransaction.setGroup(p2PDto.getGroup());
                    optimizedTransaction.setSenderUser(p2PDto.getSenderUser());
                    optimizedTransaction.setRecipientUser(p2PDto.getRecipientUser());
                    optimizedTransaction.setTransactionAmount(p2PDto.getTransactionAmount());
                    optimizedTransaction.setIsCleared(Status.PENDING);
                    optimizedTransaction.setIsUsed(Status.NOT_USED);
                    optimizedTransaction.setCreatedAt(LocalDateTime.now());
                    OptimizedTransactionEntity savedOptimizedTransaction = optimizedTransactionRepo.save(optimizedTransaction);
                    savedOptimizedTransactionIds.add(savedOptimizedTransaction.getId());

                    for (RequiresTransactionEntity transaction : p2PDto.getDuringOptimizationUsed()) {
                        OptimizedTransactionDetailsEntity details = new OptimizedTransactionDetailsEntity();
                        details.setOptimizedTransactionDetailUUID(uuidHelper.UUIDForOptimizedTransactionsDetail());
                        details.setOptimizedTransaction(optimizedTransaction);
                        details.setRequiresTransaction(transaction);
                        optimizedTransactionDetailsRepo.save(details);
                    }
                } else {
                    // totalAmount가 0인 경우, 모든 거래의 상태를 CLEAR로 설정
                    for (RequiresTransactionEntity transactionForClear : filteredTransactions) {
                        transactionForClear.setIsSenderStatus(Status.CLEAR);
                        transactionForClear.setIsRecipientStatus(Status.CLEAR);
                        transactionRepo.save(transactionForClear);
                    }
                }
            }

        }  return savedOptimizedTransactionIds;
    }


    private List<List<Long>> createCombinationList(GroupEntity group) throws CustomException {
        // 그룹 id 로 그룹 사용자 리스트를 조회
        List<GroupUserEntity> groupUserList = groupUserRepo.findByGroup_Id(group.getId());
        if (groupUserList.isEmpty()) {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        // GroupUserEntity 목록에서 UserEntity 목록으로 반환
        List<UserEntity> userList = groupUserList.stream()
                .map(GroupUserEntity::getUser)
                .collect(Collectors.toList());

        // Further processing
        List<Long> userFKList = userList.stream().map(UserEntity::getId).collect(Collectors.toList());

        // Make combinationList
        List<List<Long>> combinationList = new ArrayList<>();
        for (int i = 0; i < userFKList.size(); i++) {
            for (int j = i + 1; j < userFKList.size(); j++) {
                combinationList.add(List.of(userFKList.get(i), userFKList.get(j)));
            }
        }
        return combinationList;
    }


}
