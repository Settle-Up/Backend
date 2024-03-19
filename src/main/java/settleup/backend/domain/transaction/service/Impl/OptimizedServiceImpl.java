package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
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
     * @progress 1. createCombinationList
     * 2. retrievedAndOptimizationFromNode : 그룹id 를 가지고 있는 모든 requiresTransaction 중에 (is sender status, is recipient status) not clear
     * 3. 두 노드 최적화
     * 4. common  - calculate Net
     * 5. compare between calculate Net and OptimizationTransaction
     * 6. if == save
     */
    @Override
    public CompletableFuture<Void> optimizationOfp2p(TransactionDto targetDto) throws CustomException {
        return CompletableFuture.runAsync(()->{
        List<List<Long>> nodeList = createCombinationList(targetDto.getGroup());// 1,23,11,13
        System.out.println("heyNode:" + nodeList); //[[23, 1], [23, 11], [23, 13], [1, 11], [1, 13], [11, 13]]
        retrievedAndOptimizationFromNode(nodeList, targetDto.getGroup());
    });
    }

//    heyNode:[[23, 1], [23, 11], [23, 13], [1, 11], [1, 13], [11, 13]]


    private void retrievedAndOptimizationFromNode(List<List<Long>> nodeList, GroupEntity group) {
        for (List<Long> pair : nodeList) {
            Long userId1 = pair.get(0);
            Long userId2 = pair.get(1);

            TransactionP2PCalculationResultDto resultDto = calculateTotalAmountFromTransactions(userId1, userId2);
            double totalAmount = resultDto.getTotalAmount();
            List<RequiresTransactionEntity> allTransactions = resultDto.getAllTransactions();

            if (!allTransactions.isEmpty() || totalAmount != 0) {
                UserEntity sender, recipient;
                if (totalAmount >= 0) {
                    recipient = userRepo.findById(userId1)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    sender = userRepo.findById(userId2)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                } else {
                    sender = userRepo.findById(userId1)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    recipient = userRepo.findById(userId2)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    totalAmount = -totalAmount; // Convert the amount to positive for recording
                }

                OptimizedTransactionEntity optimization = new OptimizedTransactionEntity();
                String optimizationUUID = uuidHelper.UUIDForOptimizedTransaction();
                optimization.setOptimizedTransactionUUID(optimizationUUID);
                optimization.setGroup(group);
                optimization.setSenderUser(sender);
                optimization.setRecipientUser(recipient);
                optimization.setTransactionAmount(totalAmount);
                optimization.setIsCleared(Status.PENDING);
                optimization.setCreatedAt(LocalDateTime.now());
                optimizedTransactionRepo.save(optimization);


                for (RequiresTransactionEntity transaction : allTransactions) {
                    transaction.setIsSenderStatus(Status.CLEAR);
                    transaction.setIsRecipientStatus(Status.CLEAR);
                    requireTransactionRepo.save(transaction);


                    OptimizedTransactionDetailsEntity details = new OptimizedTransactionDetailsEntity();
                    details.setOptimizedTransactionDetailUUID(uuidHelper.UUIDForOptimizedTransactionsDetail());
                    details.setOptimizedTransactionEntity(optimization);
                    details.setRequiresTransaction(transaction);
                    optimizedTransactionDetailsRepo.save(details);
                    }
                }
            }
        }
//    Sender: 23, Recipient: 1
//    Sender: 23, Recipient: 11
//    Sender: 23, Recipient: 13
//    Sender: 1, Recipient: 11
//    Sender: 1, Recipient: 13
//    Sender: 11, Recipient: 13

        private TransactionP2PCalculationResultDto calculateTotalAmountFromTransactions (Long senderUserId, Long
        recipientUserId){
            // Transactions from user1 to user2
            List<RequiresTransactionEntity> transactionsFromUser1ToUser2 = transactionRepo.findBySenderUser_IdAndRecipientUser_Id(senderUserId, recipientUserId);

            // Transactions from user2 to user1
            List<RequiresTransactionEntity> transactionsFromUser2ToUser1 = transactionRepo.findByRecipientUser_IdAndSenderUser_Id(recipientUserId, senderUserId);

            // Combine and calculate the net amount
            double totalAmount = Stream.concat(transactionsFromUser1ToUser2.stream(), transactionsFromUser2ToUser1.stream())
                    .mapToDouble(t -> t.getSenderUser().getId().equals(senderUserId) ? t.getTransactionAmount() : -t.getTransactionAmount())
                    .sum();


            // Combine all transactions
            List<RequiresTransactionEntity> allTransactions = new ArrayList<>(transactionsFromUser1ToUser2);
            allTransactions.addAll(transactionsFromUser2ToUser1);

            return new TransactionP2PCalculationResultDto(totalAmount, allTransactions);
        }
        //    Sender: 23, Recipient: 1
//    Sender: 23, Recipient: 11
//    Sender: 23, Recipient: 13
//    Sender: 1, Recipient: 11
//    Sender: 1, Recipient: 13
//    Sender: 11, Recipient: 13

//private TransactionP2PCalculationResultDto calculateTotalAmountFromTransactions(Long senderUserId, Long recipientUserId) {
//        Long senderUser_Id=senderUserId;
//        Long recipientUser_Id=recipientUserId;
//
//    // Transactions from user1 to user2
//    List<RequiresTransactionEntity> transactionsFromUser1ToUser2 = transactionRepo.findBySenderUser_IdAndRecipientUser_Id(senderUser_Id, recipientUser_Id);
//    if (!transactionsFromUser1ToUser2.isEmpty()) {
//        transactionsFromUser1ToUser2.forEach(transaction ->
//                System.out.println("hey1 From User1 to User2 - Sender: " + transaction.getSenderUser().getUserName() +
//                        ", Recipient: " + transaction.getRecipientUser().getUserName() +
//                        ", Amount: " + transaction.getTransactionAmount()));
//    } else {
//        System.out.println("No transactions from User1 to User2.");
//    }
//
//    // Transactions from user2 to user1
//    List<RequiresTransactionEntity> transactionsFromUser2ToUser1 = transactionRepo.findByRecipientUser_IdAndSenderUser_Id(recipientUser_Id, senderUser_Id);
//    if (!transactionsFromUser2ToUser1.isEmpty()) {
//        transactionsFromUser2ToUser1.forEach(transaction ->
//                System.out.println("hey2 From User2 to User1 - Sender: " + transaction.getSenderUser().getUserName() +
//                        ", Recipient: " + transaction.getRecipientUser().getUserName() +
//                        ", Amount: " + transaction.getTransactionAmount()));
//    } else {
//        System.out.println("No transactions from User2 to User1.");
//    }
//
//    // Combine and calculate the net amount
//    double totalAmount = Stream.concat(transactionsFromUser1ToUser2.stream(), transactionsFromUser2ToUser1.stream())
//            .mapToDouble(t -> t.getSenderUser().getId().equals(senderUserId) ? t.getTransactionAmount() : -t.getTransactionAmount())
//            .sum();
//    System.out.println("Net Total Amount: " + totalAmount);
//
//    // Combine all transactions
//    List<RequiresTransactionEntity> allTransactions = new ArrayList<>(transactionsFromUser1ToUser2);
//    allTransactions.addAll(transactionsFromUser2ToUser1);
//
//    return new TransactionP2PCalculationResultDto(totalAmount, allTransactions);
//}



        private List<List<Long>> createCombinationList (GroupEntity group) throws CustomException {
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


//    @Override
//    public void optimizationOfNet(OptimizationTargetDto targetDto) throws CustomException {
//
//    }
//
//    @Override
//    public void optimizationOfGroup(OptimizationTargetDto targetDto) throws CustomException {
//
//    }
    }
