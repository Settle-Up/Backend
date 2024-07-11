package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.OptimizedDirectionService;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class RequireTransactionServiceImpl implements RequireTransactionService {

    private final UUID_Helper uuidHelper;
    private final OptimizedDirectionService optimizedDirectionService;
    private final ReceiptItemRepository itemRepository;
    private final ReceiptItemUserRepository itemUserRepository;
    private final RequireTransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(RequireTransactionService.class);

    @Override
    @Transactional
    public void createExpense(TransactionDto requestDto) {
        logger.debug("Starting createExpense method with requestDto: {}", requestDto);
        processTransactionItems(requestDto);
        logger.info("createExpense OK");
    }

    private void processTransactionItems(TransactionDto requestDto) {
        Boolean isUserType = requestDto.getIsUserType();
        List<ReceiptItemEntity> itemList = itemRepository.findByReceiptId(requestDto.getReceipt().getId());
        logger.debug("Fetched {} items for receiptId: {}", itemList.size(), requestDto.getReceipt().getId());

        boolean allTransactionsSaved = true;

        for (ReceiptItemEntity item : itemList) {
            logger.debug("Processing item: {}", item);
            List<ReceiptItemUserEntity> itemUserList = itemUserRepository.findByReceiptItemId(item.getId());
            logger.debug("Fetched {} itemUsers for itemId: {}", itemUserList.size(), item.getId());

            for (ReceiptItemUserEntity itemUser : itemUserList) {
                if (isTransactionRequired(itemUser, requestDto)) {
                    boolean transactionSaved = processEachTransaction(itemUser, requestDto);
                    if (!transactionSaved) {
                        allTransactionsSaved = false;
                    }
                }
            }
        }

        if (allTransactionsSaved) {
            UserGroupDto userGroupDto = new UserGroupDto();
            userGroupDto.setGroup(requestDto.getGroup());
            userGroupDto.setIsUserType(isUserType);
            optimizedDirectionService.performOptimizationOperations(userGroupDto);
            logger.debug("All transactions saved, optimization operations performed.");
            logger.info("processTransactionItems OK");
        }
    }

//    private boolean processEachTransaction(ReceiptItemEntity item, ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
//        BigDecimal saveAmount = calculateSaveAmount(item, itemUser);
//        logger.debug("Processed transaction item with amount: {}, for user: {}", saveAmount, itemUser.getUser().getId());
//        return saveTransaction(itemUser, requestDto, saveAmount);
//    }

//    private BigDecimal calculateSaveAmount(ReceiptItemEntity item, ReceiptItemUserEntity itemUser) {
//        BigDecimal targetDividedValue = item.getUnitPrice().multiply(item.getItemQuantity());
//        BigDecimal saveAmount = Optional.ofNullable(itemUser.getPurchasedQuantity())
//                .map(purchasedQuantity -> targetDividedValue.multiply(purchasedQuantity).divide(item.getItemQuantity(), BigDecimal.ROUND_HALF_UP))
//                .orElse(targetDividedValue.divide(BigDecimal.valueOf(item.getJointPurchaserCount()), BigDecimal.ROUND_HALF_UP));
//        logger.debug("Calculated save amount: {} for item: {}, itemUser: {}", saveAmount, item, itemUser);
//        return saveAmount;
//    }

    private boolean processEachTransaction(ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        BigDecimal saveAmount = calculateSaveAmount(itemUser); // 변경된 부분: calculateSaveAmount 호출 방식
        logger.debug("Processed transaction item with amount: {}, for user: {}", saveAmount, itemUser.getUser().getId());
        return saveTransaction(itemUser, requestDto, saveAmount);
    }

    private BigDecimal calculateSaveAmount(ReceiptItemUserEntity itemUser) {
        BigDecimal saveAmount = Optional.ofNullable(itemUser.getPurchasedTotalAmount()) // 변경된 부분: purchasedTotalAmount 사용
                .orElse(BigDecimal.ZERO);
        logger.debug("Calculated save amount: {} for itemUser: {}", saveAmount, itemUser);
        return saveAmount;
    }

    private boolean saveTransaction(ReceiptItemUserEntity itemUser, TransactionDto requestDto, BigDecimal saveAmount) {
        try {
            RequiresTransactionEntity transaction = createTransactionEntity(itemUser, requestDto, saveAmount);
            transactionRepository.save(transaction);
            logger.debug("Transaction saved successfully for user: {}", itemUser.getUser().getId());
            return true;
        } catch (DataAccessException ex) {
            logger.error("Error saving transaction for user: {}", itemUser.getUser().getId(), ex);
            return false;
        }
    }

    private RequiresTransactionEntity createTransactionEntity(ReceiptItemUserEntity itemUser, TransactionDto requestDto, BigDecimal saveAmount) {
        RequiresTransactionEntity transaction = new RequiresTransactionEntity();
        LocalDateTime now = LocalDateTime.now();
        transaction.setTransactionUUID(uuidHelper.UUIDForTransaction());
        transaction.setReceipt(requestDto.getReceipt());
        transaction.setGroup(requestDto.getGroup());
        transaction.setRecipientUser(requestDto.getPayerUser());
        transaction.setRequiredReflection(Status.REQUIRE_OPTIMIZED);
        transaction.setSenderUser(itemUser.getUser());
        transaction.setTransactionAmount(saveAmount);
        transaction.setCreatedAt(now);
        Status userType = requestDto.getIsUserType() ? Status.REGULAR : Status.DEMO;
        transaction.setUserType(userType);

        logger.debug("Created transaction entity: {}", transaction);
        return transaction;
    }

    private boolean isTransactionRequired(ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        boolean required = !itemUser.getUser().getId().equals(requestDto.getReceipt().getPayerUser().getId());
        logger.debug("Transaction required check for user: {}, result: {}", itemUser.getUser().getId(), required);
        return required;
    }
}
