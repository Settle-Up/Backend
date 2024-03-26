package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.entity.dto.OptimizedDetailUUIDsDto;
import settleup.backend.domain.group.repository.GroupRepository;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class OverviewServiceImpl implements OverviewService {
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
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
        overviewDto.setGroupId(existingGroup.get().getGroupUUID());
        overviewDto.setGroupName(existingGroup.get().getGroupName());

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setGroup(existingGroup.get());
        List<NetDto> netDtoList = netService.calculateNet(transactionDto);


        netDtoList.stream()
                .filter(netDto -> netDto.getUser().equals(existingUser.get()))
                .findFirst()
                .ifPresent(netDto -> overviewDto.setSettlementBalance(String.valueOf(netDto.getNetAmount())));


        List<GroupOverviewDto.OverviewTransactionDto> combinedTransactionList = new ArrayList<>();


        OptimizedDetailUUIDsDto searchUUIDForOptimized = mergeOptimizedAndProcessAndExtractUUIDs(existingGroup.get(), existingUser.get(), finalOptimizedDetailRepo, finalOptimizedRepo);
        List<FinalOptimizedTransactionEntity> finalOptimizedTransactionList =
                finalOptimizedRepo.findByGroupAndUserAndStatusNotUsedAndNotCleared(existingGroup.get(), existingUser.get());
        if (finalOptimizedTransactionList != null && !finalOptimizedTransactionList.isEmpty()) {
            List<GroupOverviewDto.OverviewTransactionDto> overviewTransaction1st =
                    processTransactions(existingUser.get().getId(), finalOptimizedTransactionList, userRepo);
            combinedTransactionList.addAll(overviewTransaction1st);
            System.out.println("here:" + overviewTransaction1st);

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
                Optional<OptimizedTransactionEntity> result = Optional.ofNullable(optimizedRepo.findByTransactionUUID(search));
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
        buildExpenseList(existingGroup,existingUser,overviewDto);
        buildLastWeekSettledTransactionList(existingGroup,existingUser,overviewDto);
        return overviewDto;
    }

    private void buildLastWeekSettledTransactionList(Optional<GroupEntity> existingGroup, Optional<UserEntity> existingUser, GroupOverviewDto overviewDto) {
        if (overviewDto.getLastWeekSettledTransactionList() == null) {
            overviewDto.setLastWeekSettledTransactionList(new ArrayList<>());
        }
        // require_transaction 에서

    }

    private void buildExpenseList(Optional<GroupEntity> existingGroup, Optional<UserEntity> existingUser,GroupOverviewDto overviewDto) {
        if (overviewDto.getExpenseList() == null) {
            overviewDto.setExpenseList(new ArrayList<>());
        }
        List<ReceiptEntity> groupReceiptList = receiptRepo.findReceiptByGroupId(existingGroup.get().getId());
        for(ReceiptEntity expense: groupReceiptList){
            GroupOverviewDto.ExpenseDto expenseTransaction = new GroupOverviewDto.ExpenseDto();
            expenseTransaction.setReceiptId(expense.getReceiptUUID());
            expenseTransaction.setReceiptName(expense.getReceiptName());
            expenseTransaction.setCreateAt(String.valueOf(expense.getCreatedAt()));
            expenseTransaction.setPayerUserId(expense.getPayerUser().getUserUUID());
            expenseTransaction.setPayerUserName(expense.getPayerUser().getUserName());
            expenseTransaction.setTotalAmount(String.valueOf(expense.getActualPaidPrice()));
            List<RequiresTransactionEntity> requireExpenseList = requireTransactionRepo.findByReceiptId(expense.getId());
            if(existingUser.get().getId() == expense.getPayerUser().getId()){
                Double totalAmountForRecipient = requireExpenseList.stream()
                        .filter(transaction -> transaction.getRecipientUser().getId().equals(existingUser.get().getId()))
                        .mapToDouble(RequiresTransactionEntity::getTransactionAmount)
                        .sum();
                expenseTransaction.setUserOwedAmount(String.valueOf(totalAmountForRecipient));
            } else {
                Double totalAmountForSender = requireExpenseList.stream()
                        .filter(transaction -> transaction.getSenderUser().getId().equals(existingUser.get().getId()))
                        .mapToDouble(RequiresTransactionEntity::getTransactionAmount)
                        .sum();
                if (totalAmountForSender != 0) {
                    expenseTransaction.setUserOwedAmount(String.valueOf(-totalAmountForSender));
                } else {
                    expenseTransaction.setUserOwedAmount(String.valueOf(totalAmountForSender));
                }
            }

            overviewDto.getExpenseList().add(expenseTransaction);
        }

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
            if (transaction.getSenderUser().getId().equals(userId)) {
                counterPartyId = transaction.getRecipientUser().getId();
                transactionDirection = Status.OWE;
            } else if (!transaction.getSenderUser().getId().equals(userId) && transaction.getRecipientUser().getId().equals(userId)) {
                counterPartyId = transaction.getSenderUser().getId();
                transactionDirection = Status.OWED;
            }

            GroupOverviewDto.OverviewTransactionDto overviewTransaction = new GroupOverviewDto.OverviewTransactionDto();
            overviewTransaction.setTransactionId(transaction.getTransactionUUID());
            overviewTransaction.setCounterPartyId(userRepo.findById(counterPartyId).orElseThrow().getUserUUID());
            overviewTransaction.setCounterPartyName(userRepo.findById(counterPartyId).orElseThrow().getUserName());
            overviewTransaction.setTransactionAmount(String.valueOf(transaction.getTransactionAmount()));
            overviewTransaction.setTransactionDirection(transactionDirection);
            overviewTransaction.setHasSentOrReceived(false);
            overviewTransaction.setIsReject(Status.PENDING);

            overviewTransactionList.add(overviewTransaction);
        }

        return overviewTransactionList;
    }

}
