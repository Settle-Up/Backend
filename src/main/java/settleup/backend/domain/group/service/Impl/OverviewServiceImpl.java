package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.entity.dto.GroupOverviewExpenseDto;
import settleup.backend.domain.group.entity.dto.OptimizedDetailUUIDsDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.OverviewService;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
    private final FinalOptimizedTransactionRepository finalOptimizedRepo;
    private final FinalOptimizedTransactionDetailRepository finalOptimizedDetailRepo;
    private final ReceiptRepository receiptRepo;
    private final RequireTransactionRepository requireTransactionRepo;

    @Override
    public GroupOverviewDto retrievedOverview(String groupUUID, UserInfoDto userInfoDto) throws CustomException {
        GroupOverviewDto overviewDto = new GroupOverviewDto();

        Optional<UserEntity> existingUser = Optional.ofNullable(userRepo.findByUserUUID(userInfoDto.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        overviewDto.setUserId(existingUser.get().getUserUUID());
        overviewDto.setUserName(existingUser.get().getUserName());

        Optional<GroupEntity> existingGroup = Optional.ofNullable(groupRepo.findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND)));

        Optional<GroupUserEntity> existingUserInGroup = Optional.ofNullable(groupUserRepo.findByUserIdAndGroupId(existingUser.get().getId(), existingGroup.get().getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        overviewDto.setGroupId(existingGroup.get().getGroupUUID());
        overviewDto.setGroupName(existingGroup.get().getGroupName());

        existingUserInGroup
                .ifPresent(groupUserEntity -> {
                    overviewDto.setIsMonthlyReportUpdateOn(groupUserEntity.getIsMonthlyReportUpdateOn());
                });


        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setGroup(existingGroup.get());
        List<NetDto> netDtoList = netService.calculateNet(transactionDto);


        netDtoList.stream()
                .filter(netDto -> netDto.getUser().equals(existingUser.get()))
                .findFirst()
                .ifPresent(netDto -> {
                    String formattedNetAmount = String.format("%.2f", netDto.getNetAmount());
                    overviewDto.setSettlementBalance(formattedNetAmount);
                });


        List<GroupOverviewDto.OverviewTransactionDto> combinedTransactionList = new ArrayList<>();


        OptimizedDetailUUIDsDto searchUUIDForOptimized = mergeOptimizedAndProcessAndExtractUUIDs(existingGroup.get(), existingUser.get(), finalOptimizedDetailRepo, finalOptimizedRepo);
        List<FinalOptimizedTransactionEntity> finalOptimizedTransactionList =
                finalOptimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotCleared(existingGroup.get(), existingUser.get());
        if (finalOptimizedTransactionList != null && !finalOptimizedTransactionList.isEmpty()) {
            List<GroupOverviewDto.OverviewTransactionDto> overviewTransaction1st =
                    processTransactions(existingUser.get().getId(), finalOptimizedTransactionList, userRepo);
            combinedTransactionList.addAll(overviewTransaction1st);

        }


        // 2번과정 groupOptimizedRepo 조회 시 null 값이라면 3번 과정으로 간다
        List<GroupOptimizedTransactionEntity> groupOptimizedTransactionForDetails = groupOptimizedRepo.
                findByGroupAndUserAndStatusNotUsedAndNotCleared(existingGroup.get(), existingUser.get());

        List<OptimizedTransactionEntity> searchForOptimizedFromGroupIn =
                groupOptimizedDetailRepo.findOptimizedTransactionsByGroupOptimizedTransactions(groupOptimizedTransactionForDetails);

        List<GroupOptimizedTransactionEntity> groupOptimizedTransactionList = new ArrayList<>();
        if (searchUUIDForOptimized.getSearchUUIDForGroupOptimized() != null &&
                !searchUUIDForOptimized.getSearchUUIDForGroupOptimized().isEmpty()) {
            groupOptimizedTransactionList =
                    groupOptimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingUUIDs(
                            existingGroup.get(), existingUser.get(), searchUUIDForOptimized.getSearchUUIDForGroupOptimized());
        } else {
            groupOptimizedTransactionList =
                    groupOptimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotCleared(existingGroup.get(), existingUser.get());
        }

        if (groupOptimizedTransactionList != null && !groupOptimizedTransactionList.isEmpty()) {
            List<GroupOverviewDto.OverviewTransactionDto> overviewTransactionDto2nd =
                    processTransactions(existingUser.get().getId(), groupOptimizedTransactionList, userRepo);
            combinedTransactionList.addAll(overviewTransactionDto2nd);
        }


        List<String> searchTarget = searchUUIDForOptimized.getSearchUUIDForOptimized();
        if (searchTarget != null && !searchTarget.isEmpty()) {
            for (String search : searchTarget) {
                Optional<OptimizedTransactionEntity> result = optimizedRepo.findByTransactionUUID(search);
                result.ifPresent(searchForOptimizedFromGroupIn::add);
            }
        }


        List<Long> transactionIds = searchForOptimizedFromGroupIn.stream()
                .map(OptimizedTransactionEntity::getId)
                .collect(Collectors.toList());


        //3번째 과정
        List<OptimizedTransactionEntity> optimizedTransactionList = new ArrayList<>();
        if (!searchForOptimizedFromGroupIn.isEmpty()) {
            optimizedTransactionList = optimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingTransactions(
                    existingGroup.get(), existingUser.get(), transactionIds);
        } else {
            optimizedTransactionList = optimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotCleared(existingGroup.get(), existingUser.get());
        }

        List<GroupOverviewDto.OverviewTransactionDto> overviewTransactionDto3nd =
                processTransactions(existingUser.get().getId(), optimizedTransactionList, userRepo);
        combinedTransactionList.addAll(overviewTransactionDto3nd);


        overviewDto.setNeededTransactionList(combinedTransactionList);
        buildLastWeekSettledTransactionList(existingGroup, existingUser, overviewDto);
        return overviewDto;
    }


    private void buildLastWeekSettledTransactionList(Optional<GroupEntity> existingGroup, Optional<UserEntity> existingUser, GroupOverviewDto overviewDto) {
        if (overviewDto.getLastWeekSettledTransactionList() == null) {
            overviewDto.setLastWeekSettledTransactionList(new ArrayList<>());
        }

        LocalDateTime startDate = LocalDateTime.now().minusWeeks(1);
        List<FinalOptimizedTransactionEntity> clearListFromFinal = finalOptimizedRepo.findByGroupAndUserWithStatusClearAndTransactionsSinceLastWeek(existingGroup.get(), existingUser.get(), startDate);
        List<GroupOptimizedTransactionEntity> clearListFromGroup = groupOptimizedRepo.findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(existingGroup.get(), existingUser.get(), startDate);
        List<OptimizedTransactionEntity> clearListFromOptimized = optimizedRepo.findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(existingGroup.get(), existingUser.get(), startDate);

        List<GroupOverviewDto.OverviewTransactionDto> combinedList = new ArrayList<>();

        for (FinalOptimizedTransactionEntity transaction : clearListFromFinal) {
            combinedList.add(convertToOverviewTransactionDto(transaction, existingUser));
        }
        for (GroupOptimizedTransactionEntity transaction : clearListFromGroup) {
            combinedList.add(convertToOverviewTransactionDto(transaction, existingUser));
        }
        for (OptimizedTransactionEntity transaction : clearListFromOptimized) {
            combinedList.add(convertToOverviewTransactionDto(transaction, existingUser));
        }
        combinedList.sort(Comparator.comparing(dto -> dto.getClearedAt() == null ? LocalDateTime.MAX : LocalDateTime.parse(dto.getClearedAt())));

        overviewDto.setLastWeekSettledTransactionList(combinedList);
    }


    private GroupOverviewDto.OverviewTransactionDto convertToOverviewTransactionDto(TransactionalEntity transaction, Optional<UserEntity> existingUser) {
        GroupOverviewDto.OverviewTransactionDto dto = new GroupOverviewDto.OverviewTransactionDto();
        dto.setTransactionId(transaction.getTransactionUUID());
        String formattedTransactionAmount = String.format("%.2f", transaction.getTransactionAmount());
        dto.setTransactionAmount(formattedTransactionAmount);
        LocalDateTime clearedAt = transaction.getClearStatusTimeStamp();
        dto.setClearedAt(clearedAt == null ? null : clearedAt.toString());
        dto.setHasSentOrReceived(true);
        dto.setIsRejected(null);

        Long userId = existingUser.get().getId();
        if (transaction.getSenderUser().getId().equals(userId)) {
            dto.setCounterPartyId(transaction.getRecipientUser().getUserUUID());
            dto.setCounterPartyName(transaction.getRecipientUser().getUserName());
            dto.setTransactionDirection(Status.OWE);
        } else if (transaction.getRecipientUser().getId().equals(userId)) {
            dto.setCounterPartyId(transaction.getSenderUser().getUserUUID());
            dto.setCounterPartyName(transaction.getSenderUser().getUserName());
            dto.setTransactionDirection(Status.OWED);
        }

        return dto;
    }


    private GroupOverviewExpenseDto buildExpenseList(Optional<GroupEntity> existingGroup, Optional<UserEntity> existingUser, GroupOverviewExpenseDto groupOverviewExpenseDto, Pageable pageable) {

        GroupOverviewExpenseDto.ExpenseDto expenseListDto = new GroupOverviewExpenseDto.ExpenseDto();
        groupOverviewExpenseDto.setExpenses(new ArrayList<>());

        Page<ReceiptEntity> pagedReceipts = receiptRepo.findReceiptByGroupId(existingGroup.get().getId(), pageable);
        for (ReceiptEntity expense : pagedReceipts) {
            GroupOverviewExpenseDto.ExpenseDto expenseTransaction = new GroupOverviewExpenseDto.ExpenseDto();
            expenseTransaction.setReceiptId(expense.getReceiptUUID());
            expenseTransaction.setReceiptName(expense.getReceiptName());
            expenseTransaction.setCreatedAt(String.valueOf(expense.getCreatedAt()));
            expenseTransaction.setPayerUserId(expense.getPayerUser().getUserUUID());
            expenseTransaction.setPayerUserName(expense.getPayerUser().getUserName());
            expenseTransaction.setTotalPrice(String.format("%.2f", expense.getActualPaidPrice()));

            List<RequiresTransactionEntity> requireExpenseList = requireTransactionRepo.findByReceiptId(expense.getId());
            Double totalAmountForCurrentUser = requireExpenseList.stream()
                    .filter(transaction -> transaction.getRecipientUser().getId().equals(existingUser.get().getId()) || transaction.getSenderUser().getId().equals(existingUser.get().getId()))
                    .mapToDouble(transaction -> transaction.getRecipientUser().getId().equals(existingUser.get().getId()) ? transaction.getTransactionAmount() : -transaction.getTransactionAmount())
                    .sum();
            expenseTransaction.setUserOwedAmount(String.format("%.2f", totalAmountForCurrentUser));

            groupOverviewExpenseDto.getExpenses().add(expenseTransaction);
        }

        groupOverviewExpenseDto.setHasNextPage(pagedReceipts.hasNext());
        return groupOverviewExpenseDto;
    }

    @Override
    public GroupOverviewExpenseDto updateRetrievedExpenseList(GroupOverviewExpenseDto groupOverviewExpenseDto, String groupUUID, UserInfoDto userInfoDto, Pageable pageable) throws CustomException {
        Optional<GroupEntity> existingGroup = Optional.ofNullable(groupRepo.findByGroupUUID(groupUUID))
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        Optional<UserEntity> existingUser = Optional.ofNullable(userRepo.findByUserUUID(userInfoDto.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Optional.ofNullable(groupUserRepo.findByUserIdAndGroupId(existingUser.get().getId(), existingGroup.get().getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        return buildExpenseList(existingGroup, existingUser, groupOverviewExpenseDto, pageable);
    }


    private OptimizedDetailUUIDsDto mergeOptimizedAndProcessAndExtractUUIDs(GroupEntity group, UserEntity
            user, FinalOptimizedTransactionDetailRepository finalOptimizedDetailRepo, FinalOptimizedTransactionRepository
                                                                                    finalOptimizedRepo) {
        List<FinalOptimizedTransactionEntity> finalOptimizedTransactionList =
                finalOptimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotCleared(group, user);

        List<String> searchUUIDForGroupOptimized = new ArrayList<>();
        List<String> searchUUIDForOptimized = new ArrayList<>();

        if (finalOptimizedTransactionList != null && !finalOptimizedTransactionList.isEmpty()) {
            List<FinalOptimizedTransactionEntity> finalOptimizedListForDetail =
                    finalOptimizedRepo.findNotUsedTransactionsByGroup(group);

            for (FinalOptimizedTransactionEntity detail : finalOptimizedListForDetail) {
                List<String> uuids = finalOptimizedDetailRepo.findUsedOptimizedTransactionUuidsByFinalOptimizedTransactionId(detail.getId());
                for (String uuid : uuids) {
                    if (uuid.startsWith("GPT")) {
                        searchUUIDForGroupOptimized.add(uuid);
                    } else if (uuid.startsWith("OPT")) {
                        searchUUIDForOptimized.add(uuid);
                    }
                }
            }
        }
        return new OptimizedDetailUUIDsDto(searchUUIDForGroupOptimized, searchUUIDForOptimized);


    }


    public static List<GroupOverviewDto.OverviewTransactionDto> processTransactions(
            Long userId,
            List<? extends TransactionalEntity> transactions,
            UserRepository userRepo) {
        List<GroupOverviewDto.OverviewTransactionDto> overviewTransactionList = new ArrayList<>();

        for (TransactionalEntity transaction : transactions) {
            Long counterPartyId = null;
            Status transactionDirection = null;
            Boolean hasSentOrReceived = null;
            Boolean isReject = null;
            if (transaction.getSenderUser().getId().equals(userId)) {
                counterPartyId = transaction.getRecipientUser().getId();
                transactionDirection = Status.OWE;
                if (transaction.getIsSenderStatus() ==Status.CLEAR){
                    hasSentOrReceived =true;
                } else {
                    hasSentOrReceived =false;
                }
                if (transaction.getIsRecipientStatus() == Status.REJECT) {
                    isReject = true;
                } else {
                    isReject = null;
                }

                } else if (!transaction.getSenderUser().getId().equals(userId) && transaction.getRecipientUser().getId().equals(userId)) {
                    counterPartyId = transaction.getSenderUser().getId();
                    transactionDirection = Status.OWED;
                if (transaction.getIsRecipientStatus() ==Status.CLEAR){
                    hasSentOrReceived =true;
                } else {
                    hasSentOrReceived =false;
                }
                if (transaction.getIsSenderStatus() == Status.REJECT) {
                    isReject = true;
                } else {
                    isReject = null;
                }

                }

                GroupOverviewDto.OverviewTransactionDto overviewTransaction = new GroupOverviewDto.OverviewTransactionDto();
                overviewTransaction.setTransactionId(transaction.getTransactionUUID());
                overviewTransaction.setCounterPartyId(userRepo.findById(counterPartyId).orElseThrow().getUserUUID());
                overviewTransaction.setCounterPartyName(userRepo.findById(counterPartyId).orElseThrow().getUserName());
                String formattedTransactionAmount = String.format("%.2f", transaction.getTransactionAmount());
                overviewTransaction.setTransactionAmount(formattedTransactionAmount);
                overviewTransaction.setTransactionDirection(transactionDirection);
                overviewTransaction.setHasSentOrReceived(hasSentOrReceived);
                overviewTransaction.setIsRejected(isReject);

                overviewTransactionList.add(overviewTransaction);
            }

            return overviewTransactionList;
        }

    }
