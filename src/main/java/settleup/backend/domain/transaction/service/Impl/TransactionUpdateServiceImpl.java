package settleup.backend.domain.transaction.service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;
import settleup.backend.domain.transaction.model.TransactionalEntity;
import settleup.backend.domain.transaction.repository.UltimateOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.TransactionProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.service.TransactionUpdateService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Selector.TransactionStrategySelector;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class TransactionUpdateServiceImpl implements TransactionUpdateService {
    private final GroupRepository groupRepo;
    private final GroupUserRepository groupUserRepo;
    private final TransactionStrategySelector strategySelector;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final UltimateOptimizedTransactionRepository ultimateOptimizedTransactionRepo;
    private final UserRepoSelector selector;

    private static final Logger log = LoggerFactory.getLogger(TransactionUpdateServiceImpl.class);

    @Override
    @Transactional
    public TransactionUpdateDto transactionManage(UserInfoDto userInfoDto, String groupId, TransactionUpdateRequestDto request) throws CustomException {
        Boolean isUserType = userInfoDto.getIsRegularUserOrDemoUser();
        UserTypeEntity existingUser = selector.getUserRepository(isUserType).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupTypeEntity existingGroup = selector.getGroupRepository(isUserType).findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        selector.getGroupUserRepository(isUserType).findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        String goesNextOrNot = strategySelector.selectRepository(request);

        if ("save-success".equals(goesNextOrNot)) {
            TransactionProcessingService processingService = strategySelector.selectService(request.getTransactionId());
            TransactionalEntity result = processingService.processTransaction(request, existingGroup);

            TransactionUpdateDto transactionUpdateDto = new TransactionUpdateDto();
            transactionUpdateDto.setUserId(existingUser.getUserUUID());
            transactionUpdateDto.setUserName(existingUser.getUserName());

            UserTypeEntity counterParty;
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
        Boolean isUserType = userInfoDto.getIsRegularUserOrDemoUser();
        UserTypeEntity existingUser = selector.getUserRepository(isUserType).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<GroupUserTypeEntity> userInGroupList = (List<GroupUserTypeEntity>) selector.getGroupUserRepository(isUserType).findByUser_Id(existingUser.getId());

        List<TransactionalEntity> combinateList = new ArrayList<>();
        TransactionUpdateDto transactionUpdateDto = new TransactionUpdateDto();
        transactionUpdateDto.setUserId(existingUser.getUserUUID());
        transactionUpdateDto.setUserName(existingUser.getUserName());
        transactionUpdateDto.setTransactionUpdateList(new ArrayList<>());

        for (GroupUserTypeEntity groupUser : userInGroupList) {
            GroupTypeEntity existingGroup = selector.getGroupRepository(isUserType).findById(groupUser.getGroup().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));
            log.info("Processing group: {}", existingGroup.getGroupName());

            combinateList.addAll(filterOneSideClearInOptimizedTransaction(existingGroup,  existingUser));
            combinateList.addAll(filterOneSideClearInGroupOptimizedTransaction( existingGroup, existingUser));
            combinateList.addAll(filterOneSideClearInUltimateOptimizedTransaction( existingGroup,  existingUser));
        }

        for (TransactionalEntity transaction : combinateList) {
            TransactionUpdateDto.TransactionListDto transactionListDto = createTransactionListDto(transaction, existingUser);
            transactionUpdateDto.getTransactionUpdateList().add(transactionListDto);
        }

        return transactionUpdateDto;
    }


    private TransactionUpdateDto.TransactionListDto createTransactionListDto(TransactionalEntity transaction, UserTypeEntity user) {
        UserTypeEntity counterParty = transaction.getSenderUser().equals(user) ? transaction.getRecipientUser() : transaction.getSenderUser();
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

    private List<TransactionalEntity> filterOneSideClearInUltimateOptimizedTransaction(GroupTypeEntity existingGroup, UserTypeEntity existingUser) {
        return ultimateOptimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }

    private List<TransactionalEntity> filterOneSideClearInGroupOptimizedTransaction(GroupTypeEntity existingGroup, UserTypeEntity existingUser) {
        return groupOptimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }

    private List<TransactionalEntity> filterOneSideClearInOptimizedTransaction(GroupTypeEntity existingGroup, UserTypeEntity existingUser) {
        return optimizedTransactionRepo.findTransactionsForRecipientUserWithSentNotChecked(existingUser.getId(), existingGroup.getId());
    }
}
