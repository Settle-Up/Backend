package settleup.backend.domain.transaction.service.Impl;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.transaction.entity.UltimateOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.repository.UltimateOptimizedTransactionRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private final UltimateOptimizedTransactionRepository ultimateOptimizedTransactionRepo;

    private static final Logger log = LoggerFactory.getLogger(TransactionUpdateServiceImpl.class);

    /**
     * if / sender 가 들어오면 => repository 의 hassentStatus를 true 로 바꿈
     * if / receiptent 가 들어오면 => repository 의 hascheckstatus 를 true 로 바꿈
     */

    @Override
    @Transactional
    public TransactionUpdateDto transactionManage(UserInfoDto userInfoDto, String groupId, TransactionUpdateRequestDto request) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));



        String goesNextOrNot = strategySelector.selectRepository(request);

        if ("save-success".equals(goesNextOrNot)) {
            TransactionProcessingService processingService = strategySelector.selectService(request.getTransactionId());
            TransactionalEntity result = processingService.processTransaction(request, existingGroup);

            TransactionUpdateDto transactionUpdateDto = new TransactionUpdateDto();
            transactionUpdateDto.setUserId(existingUser.getUserUUID());
            transactionUpdateDto.setUserName(existingUser.getUserName());

            UserEntity counterParty;
            Status transactionDirection;

            if ("sender".equals(request.getApprovalUser())) {
                transactionDirection = Status.OWE;
                counterParty = result.getRecipientUser();
            } else {
                transactionDirection = Status.OWED;
                counterParty = result.getSenderUser();

            }

            if (transactionUpdateDto.getTransactionUpdateList() == null) {
                transactionUpdateDto.setTransactionUpdateList(new ArrayList<>());
            }

            LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
            TransactionUpdateDto.TransactionListDto transactionListDto = new TransactionUpdateDto.TransactionListDto();
            transactionListDto.setGroupId(existingGroup.getGroupUUID());
            transactionListDto.setGroupName(existingGroup.getGroupName());
            transactionListDto.setTransactionId(result.getTransactionUUID());
            transactionListDto.setCounterPartyId(counterParty.getUserUUID());
            transactionListDto.setCounterPartyName(counterParty.getUserName());
            transactionListDto.setClearedAt(String.valueOf(newClearStatusTimestamp));
            transactionListDto.setTransactionDirection(String.valueOf(transactionDirection));
            String formattedTransactionAmount = String.format("%.2f", result.getTransactionAmount());
            transactionListDto.setTransactionAmount(formattedTransactionAmount);

            transactionUpdateDto.getTransactionUpdateList().add(transactionListDto);
            return transactionUpdateDto;
        }
        throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
    }

    @Override
    public TransactionUpdateDto retrievedReceivedListInGroup(UserInfoDto userInfoDto) throws CustomException {
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
                    filterOneSideClearInUltimateOptimizedTransaction(existingGroup, existingUser);
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


        TransactionUpdateDto.TransactionListDto transactionListDto = new TransactionUpdateDto.TransactionListDto();
        transactionListDto.setTransactionId(transaction.getTransactionUUID());
        transactionListDto.setGroupId(transaction.getGroup().getGroupUUID());
        transactionListDto.setGroupName(transaction.getGroup().getGroupName());
        transactionListDto.setCounterPartyId(counterParty.getUserUUID());
        transactionListDto.setCounterPartyName(counterParty.getUserName());
        transactionListDto.setClearedAt(String.valueOf(transaction.getClearStatusTimeStamp()));
        transactionListDto.setTransactionDirection(String.valueOf(transactionDirection));
        String formattedTransactionAmount = String.format("%.2f", transaction.getTransactionAmount());
        transactionListDto.setTransactionAmount(formattedTransactionAmount);

        return transactionListDto;
    }

    private List<TransactionalEntity> filterOneSideClearInUltimateOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        return ultimateOptimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }


    private List<TransactionalEntity> filterOneSideClearInGroupOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        return groupOptimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }

    private List<TransactionalEntity> filterOneSideClearInOptimizedTransaction(GroupEntity existingGroup, UserEntity existingUser) {
        return optimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }

}

