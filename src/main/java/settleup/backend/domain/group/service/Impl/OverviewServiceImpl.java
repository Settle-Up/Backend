package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.entity.dto.GroupOverviewExpenseDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.OverviewService;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.model.TransactionalEntity;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class OverviewServiceImpl implements OverviewService {
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final GroupUserRepository groupUserRepo;
    private final NetService netService;
    private final OptimizedTransactionRepository optimizedRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedRepo;
    private final UltimateOptimizedTransactionRepository ultimateOptimizedRepo;
    private final ReceiptRepository receiptRepo;
    private final RequireTransactionRepository requireTransactionRepo;
    private final UserRepoSelector selector;

    private static final Logger log = LoggerFactory.getLogger(OverviewServiceImpl.class);

    @Override
    public GroupOverviewDto retrievedOverview(String groupUUID, UserInfoDto userInfoDto) throws CustomException {
        UserGroupDto userGroupDto = isValidUserGroup(groupUUID, userInfoDto);
        GroupOverviewDto overviewDto = buildInitiateResponseDto(userGroupDto);
        buildNeededTransactionResponseDto(overviewDto, userGroupDto);
        return buildLastWeekSettledTransactionList(overviewDto, userGroupDto);
    }


    private UserGroupDto isValidUserGroup(String groupUUID, UserInfoDto userInfoDto) {
        Boolean getSelector = userInfoDto.getIsRegularUserOrDemoUser();
        UserTypeEntity existingUser = selector.getUserRepository(getSelector).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupTypeEntity existingGroup = selector.getGroupRepository(getSelector).findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        GroupUserTypeEntity existingUserInGroup = selector.getGroupUserRepository(getSelector).findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        UserGroupDto validUserGroupDto = new UserGroupDto();
        validUserGroupDto.setGroup(existingGroup);
        validUserGroupDto.setGroupUser(existingUserInGroup);
        validUserGroupDto.setSingleUser(existingUser);
        return validUserGroupDto;


    }

    private GroupOverviewDto buildInitiateResponseDto(UserGroupDto userGroupDto) {
        UserTypeEntity user = userGroupDto.getSingleUser();
        GroupTypeEntity group = userGroupDto.getGroup();
        GroupUserTypeEntity userInGroupBridgeInfo = userGroupDto.getGroupUser();

        GroupOverviewDto resultDto = new GroupOverviewDto();
        resultDto.setUserId(user.getUserUUID());
        resultDto.setUserName(user.getUserName());
        resultDto.setGroupId(group.getGroupUUID());
        resultDto.setGroupName(group.getGroupName());
        resultDto.setIsMonthlyReportUpdateOn(userInGroupBridgeInfo.getIsMonthlyReportUpdateOn());

        boolean isReceiptRegistered = netService.isReceiptRegisteredInGroup(userGroupDto.getGroup().getId());
        log.debug("Is receipt registered for group {}: {}", userGroupDto.getGroup().getGroupUUID(), isReceiptRegistered);

        String formattedNetAmount = null;

        if (isReceiptRegistered) {
            List<NetDto> netDtoList = netService.calculateNet(userGroupDto);
            log.debug("Net calculation result for group {}: {}", userGroupDto.getGroup().getGroupUUID(), netDtoList);

            formattedNetAmount = netDtoList.stream()
                    .filter(netDto -> {
                        boolean isEqual = netDto.getUser().equals(user);
                        log.debug("Comparing user in NetDto: {}, with user: {}, isEqual: {}", netDto.getUser().getId(), user.getId(), isEqual);
                        return isEqual;
                    })
                    .findFirst()
                    .map(netDto -> {
                        String amountFormatted = String.format("%.2f", netDto.getNetAmount());
                        log.debug("Net amount found for user {}: {}", user.getId(), amountFormatted);
                        return amountFormatted;
                    })
                    .orElse("0.00");
        } else {
            formattedNetAmount = null;
        }

        log.debug("Final settlement balance for group {}: {}", userGroupDto.getGroup().getGroupUUID(), formattedNetAmount);

        resultDto.setSettlementBalance(formattedNetAmount);
        return resultDto;
    }

    private void buildNeededTransactionResponseDto(GroupOverviewDto overviewDto, UserGroupDto userGroupInfo) {
        UserTypeEntity user = userGroupInfo.getSingleUser();
        GroupTypeEntity group = userGroupInfo.getGroup();

        List<TransactionalEntity> neededTransactions = new ArrayList<>();
        neededTransactions.addAll(ultimateOptimizedRepo.findFilteredTransactions(group, user));
        neededTransactions.addAll(groupOptimizedRepo.findFilteredTransactions(group, user));
        neededTransactions.addAll(optimizedRepo.findFilteredTransactions(group, user));

        processConvertNeededTransaction(overviewDto, neededTransactions, user);
    }

    private void processConvertNeededTransaction(GroupOverviewDto buildDto, List<TransactionalEntity> neededTransactions, UserTypeEntity user) {
        if (buildDto.getNeededTransactionList() == null) {
            buildDto.setNeededTransactionList(new ArrayList<>());
        }

        for (TransactionalEntity transaction : neededTransactions) {
            GroupOverviewDto.OverviewTransactionDto neededTransactionDto = new GroupOverviewDto.OverviewTransactionDto();

            UserTypeEntity counterParty = (transaction.getSenderUser().getId().equals(user.getId())) ? transaction.getRecipientUser() : transaction.getSenderUser();
            Status transactionDirection = (transaction.getSenderUser().getId().equals(user.getId())) ? Status.OWE : Status.OWED;

            neededTransactionDto.setTransactionId(transaction.getTransactionUUID());
            neededTransactionDto.setCounterPartyId(counterParty.getUserUUID());
            neededTransactionDto.setCounterPartyName(counterParty.getUserName());
            neededTransactionDto.setTransactionAmount(String.format("%.2f", transaction.getTransactionAmount()));
            neededTransactionDto.setTransactionDirection(transactionDirection);

            buildDto.getNeededTransactionList().add(neededTransactionDto);
        }
    }

    private GroupOverviewDto buildLastWeekSettledTransactionList(GroupOverviewDto overviewDto, UserGroupDto userGroupInfo) {
        UserTypeEntity user = userGroupInfo.getSingleUser();
        GroupTypeEntity group = userGroupInfo.getGroup();

        LocalDateTime startDate = LocalDateTime.now().minusWeeks(1);
        List<TransactionalEntity> lastWeekTransactions = new ArrayList<>();
        lastWeekTransactions.addAll(ultimateOptimizedRepo.findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(group, user, startDate));
        lastWeekTransactions.addAll(groupOptimizedRepo.findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(group, user, startDate));
        lastWeekTransactions.addAll(optimizedRepo.findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(group, user, startDate));

        processConvertLastTransaction(overviewDto, lastWeekTransactions, user);
        return overviewDto;
    }

    private void processConvertLastTransaction(GroupOverviewDto processDto, List<TransactionalEntity> lastWeekTransaction, UserTypeEntity user) {
        if (processDto.getLastWeekSettledTransactionList() == null) {
            processDto.setLastWeekSettledTransactionList(new ArrayList<>());
        }

        for (TransactionalEntity transaction : lastWeekTransaction) {
            GroupOverviewDto.OverviewTransactionDto lastTransactionDto = new GroupOverviewDto.OverviewTransactionDto();

            UserTypeEntity counterParty = (transaction.getSenderUser().getId().equals(user.getId())) ? transaction.getRecipientUser() : transaction.getSenderUser();
            Status transactionDirection = (transaction.getSenderUser().getId().equals(user.getId())) ? Status.OWE : Status.OWED;

            lastTransactionDto.setTransactionId(transaction.getTransactionUUID());
            lastTransactionDto.setCounterPartyId(counterParty.getUserUUID());
            lastTransactionDto.setCounterPartyName(counterParty.getUserName());
            lastTransactionDto.setTransactionDirection(transactionDirection);
            lastTransactionDto.setTransactionAmount(String.format("%.2f", transaction.getTransactionAmount()));
            lastTransactionDto.setClearedAt(String.valueOf(transaction.getClearStatusTimeStamp()));

            processDto.getLastWeekSettledTransactionList().add(lastTransactionDto);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        processDto.getLastWeekSettledTransactionList().sort(Comparator.comparing(
                dto -> LocalDateTime.parse(dto.getClearedAt(), formatter),
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
    }


    @Override
    public GroupOverviewExpenseDto updateRetrievedExpenseList(GroupOverviewExpenseDto groupOverviewExpenseDto, String groupUUID, UserInfoDto userInfoDto, Pageable pageable) throws CustomException {
        Boolean getSelector = userInfoDto.getIsRegularUserOrDemoUser();
        GroupTypeEntity existingGroup = selector.getGroupRepository(getSelector).findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        UserTypeEntity existingUser = selector.getUserRepository(getSelector).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        selector.getGroupUserRepository(getSelector).findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        return buildExpenseList(existingGroup, existingUser, groupOverviewExpenseDto, pageable);
    }

    private GroupOverviewExpenseDto buildExpenseList(GroupTypeEntity existingGroup, UserTypeEntity existingUser, GroupOverviewExpenseDto groupOverviewExpenseDto, Pageable pageable) {
        groupOverviewExpenseDto.setExpenses(new ArrayList<>());

        Page<ReceiptEntity> pagedReceipts = receiptRepo.findReceiptByGroupId(existingGroup.getId(), pageable);
        for (ReceiptEntity expense : pagedReceipts) {
            GroupOverviewExpenseDto.ExpenseDto expenseTransaction = new GroupOverviewExpenseDto.ExpenseDto();
            expenseTransaction.setReceiptId(expense.getReceiptUUID());
            expenseTransaction.setReceiptName(expense.getReceiptName());
            expenseTransaction.setCreatedAt(String.valueOf(expense.getCreatedAt()));
            expenseTransaction.setPayerUserId(expense.getPayerUser().getUserUUID());
            expenseTransaction.setPayerUserName(expense.getPayerUser().getUserName());
            expenseTransaction.setTotalPrice(String.format("%.2f", expense.getActualPaidPrice()));

            List<RequiresTransactionEntity> requireExpenseList = requireTransactionRepo.findByReceiptId(expense.getId());
            BigDecimal totalAmountForCurrentUser = requireExpenseList.stream()
                    .filter(transaction -> transaction.getRecipientUser().getId().equals(existingUser.getId()) || transaction.getSenderUser().getId().equals(existingUser.getId()))
                    .map(transaction -> transaction.getRecipientUser().getId().equals(existingUser.getId()) ? transaction.getTransactionAmount() : transaction.getTransactionAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            expenseTransaction.setUserOwedAmount(String.format("%.2f", totalAmountForCurrentUser));

            groupOverviewExpenseDto.getExpenses().add(expenseTransaction);
        }

        groupOverviewExpenseDto.getExpenses().sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));

        groupOverviewExpenseDto.setHasNextPage(pagedReceipts.hasNext());
        return groupOverviewExpenseDto;
    }
}
