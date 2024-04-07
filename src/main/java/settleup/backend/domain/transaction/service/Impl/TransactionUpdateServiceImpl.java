package settleup.backend.domain.transaction.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.repository.FinalOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.TransactionProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.service.TransactionUpdateService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Transactional
public class TransactionUpdateServiceImpl implements TransactionUpdateService {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupUserRepository groupUserRepo;
    private final TransactionStrategySelector strategySelector;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final FinalOptimizedTransactionRepository mergeOptimizedTransactionRepo;

    private static final Logger log = LoggerFactory.getLogger(TransactionUpdateServiceImpl.class);


    @Override
    @Transactional
    public TransactionUpdateDto transactionUpdate(UserInfoDto userInfoDto, String groupId, TransactionUpdateRequestDto request) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        String transactionIdentity = request.getTransactionId();

        String goesNextOrNot = strategySelector.selectRepository(transactionIdentity,request);

        if("save-success".equals(goesNextOrNot)) {
            TransactionProcessingService processingService = strategySelector.selectService(transactionIdentity);
            TransactionalEntity result = processingService.processTransaction(transactionIdentity, request, existingGroup);

            TransactionUpdateDto transactionUpdateDto = new TransactionUpdateDto();
            transactionUpdateDto.setUserId(existingUser.getUserUUID());
            transactionUpdateDto.setUserName(existingUser.getUserName());

            UserEntity counterParty;
            Status transactionDirection;
            Boolean isRejected = null;
            Boolean hasSentOrReceived = "CLEAR".equals(request.getApprovalStatus());

            if ("sender".equals(request.getApprovalUser())) {
                transactionDirection = Status.OWE;
                counterParty = result.getRecipientUser();
                if (Status.REJECT.equals(result.getIsRecipientStatus())) {
                    isRejected = true;
                }
            } else {
                transactionDirection = Status.OWED;
                counterParty = result.getSenderUser();
                if (Status.REJECT.equals(result.getIsSenderStatus())) {
                    isRejected = true;
                }
            }

            if (transactionUpdateDto.getTransactionUpdateList() == null) {
                transactionUpdateDto.setTransactionUpdateList(new ArrayList<>());
            }


            TransactionUpdateDto.TransactionListDto transactionListDto = new TransactionUpdateDto.TransactionListDto();
            transactionListDto.setGroupId(existingGroup.getGroupUUID());
            transactionListDto.setGroupName(existingGroup.getGroupName());
            transactionListDto.setTransactionId(result.getTransactionUUID());
            transactionListDto.setCounterPartyId(counterParty.getUserUUID());
            transactionListDto.setCounterPartyName(counterParty.getUserName());
            transactionListDto.setTransactionDirection(String.valueOf(transactionDirection));
            transactionListDto.setHasSentOrReceived(hasSentOrReceived);
            transactionListDto.setIsRejected(isRejected);
            transactionListDto.setTransactionAmount(String.valueOf(result.getTransactionAmount()));

            transactionUpdateDto.getTransactionUpdateList().add(transactionListDto);
            return transactionUpdateDto;
        }
        throw  new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
    }

    @Override
    public TransactionUpdateDto retrievedUpdateListInGroup(UserInfoDto userInfoDto) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<GroupUserEntity> userInGroupList =
                groupUserRepo.findByUser_Id(existingUser.getId());


        List<TransactionalEntity> combinateList = new ArrayList<>();

        TransactionUpdateDto transactionUpdateDto = null;
        for (GroupUserEntity group : userInGroupList) {
            GroupEntity existingGroup =
                    groupRepo.findById(group.getGroup().getId()).orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));
            log.info("Processing group: {}", existingGroup.getGroupName());
            List<TransactionalEntity> filterOneSideClearInOptimizedTransactionList =
                    filterOneSideClearInOptimizedTransaction(existingGroup, existingUser);
            log.info("Filtered list size for optimized transactions: {}", filterOneSideClearInOptimizedTransactionList.size());
            combinateList.addAll(filterOneSideClearInOptimizedTransactionList);
            List<TransactionalEntity> filterOneSideClearInGroupOptimizedTransactionList =
                    filterOneSideClearInGroupOptimizedTransaction(existingGroup, existingUser);
            log.info("Filtered list size for optimized transactions: {}", filterOneSideClearInGroupOptimizedTransactionList.size());
            combinateList.addAll(filterOneSideClearInGroupOptimizedTransactionList);
            List<TransactionalEntity> filterOneSideClearInFinalOptimizedTransactionList =
                    filterOneSideClearInFinalOptimizedTransaction(existingGroup, existingUser);
            log.info("Filtered list size for optimized transactions: {}", filterOneSideClearInFinalOptimizedTransactionList.size());
            combinateList.addAll(filterOneSideClearInFinalOptimizedTransactionList);

            transactionUpdateDto = new TransactionUpdateDto();
            transactionUpdateDto.setUserId(existingUser.getUserUUID());
            transactionUpdateDto.setUserName(existingUser.getUserName());
            transactionUpdateDto.setTransactionUpdateList(new ArrayList<>());


            for (TransactionalEntity transaction : combinateList) {
                TransactionUpdateDto.TransactionListDto transactionListDto = createTransactionListDto(transaction, existingUser);
                transactionUpdateDto.getTransactionUpdateList().add(transactionListDto);

            }
        }
        return transactionUpdateDto;

    }

    private TransactionUpdateDto.TransactionListDto createTransactionListDto(TransactionalEntity transaction, UserEntity user) {
        UserEntity counterParty = transaction.getSenderUser().equals(user) ? transaction.getRecipientUser() : transaction.getSenderUser();
        Status transactionDirection = transaction.getSenderUser().equals(user) ? Status.OWE : Status.OWED;


        Boolean hasSentOrReceived = Status.CLEAR.equals(transaction.getSenderUser().equals(user) ? transaction.getIsSenderStatus() : transaction.getIsRecipientStatus());


        Boolean isRejected = null;
        if (Status.REJECT.equals(transaction.getSenderUser().equals(user) ? transaction.getIsRecipientStatus() : transaction.getIsSenderStatus())) {
            isRejected = Boolean.TRUE;
        }

        TransactionUpdateDto.TransactionListDto transactionListDto = new TransactionUpdateDto.TransactionListDto();
        transactionListDto.setTransactionId(transaction.getTransactionUUID());
        transactionListDto.setGroupId(transaction.getGroup().getGroupUUID());
        transactionListDto.setGroupName(transaction.getGroup().getGroupName());
        transactionListDto.setCounterPartyId(counterParty.getUserUUID());
        transactionListDto.setCounterPartyName(counterParty.getUserName());
        transactionListDto.setTransactionDirection(String.valueOf(transactionDirection));
        transactionListDto.setHasSentOrReceived(hasSentOrReceived);
        transactionListDto.setIsRejected(isRejected);
        transactionListDto.setTransactionAmount(String.valueOf(transaction.getTransactionAmount()));

        return transactionListDto;
    }

    private List<TransactionalEntity> filterOneSideClearInFinalOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        List<OptimizedTransactionEntity> transactions = optimizedTransactionRepo.findTransactionsWithOneSideClearAndNotInheritedClear(existingGroup);

        return transactions.stream()
                .filter(transaction -> {
                    if (transaction.getSenderUser().equals(existingUser) && transaction.getIsSenderStatus() == Status.PENDING) {
                        return true;
                    } else
                        return transaction.getRecipientUser().equals(existingUser) && transaction.getIsRecipientStatus() == Status.PENDING;
                })
                .collect(Collectors.toList());
    }

    private List<TransactionalEntity> filterOneSideClearInGroupOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        List<GroupOptimizedTransactionEntity> transactions = groupOptimizedTransactionRepo.findTransactionsWithOneSideClearAndNotInheritedClear(existingGroup);

        return transactions.stream()
                .filter(transaction -> {
                    if (transaction.getSenderUser().equals(existingUser) && transaction.getIsSenderStatus() == Status.PENDING) {
                        return true;
                    } else
                        return transaction.getRecipientUser().equals(existingUser) && transaction.getIsRecipientStatus() == Status.PENDING;
                })
                .collect(Collectors.toList());
    }

    private List<TransactionalEntity> filterOneSideClearInOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        List<FinalOptimizedTransactionEntity> transactions = mergeOptimizedTransactionRepo.findTransactionsWithOneSideClear(existingGroup);

        return transactions.stream()
                .filter(transaction -> {
                    if (transaction.getSenderUser().equals(existingUser) && transaction.getIsSenderStatus() == Status.PENDING) {
                        return true;
                    } else
                        return transaction.getRecipientUser().equals(existingUser) && transaction.getIsRecipientStatus() == Status.PENDING;
                })
                .collect(Collectors.toList());
    }
}


