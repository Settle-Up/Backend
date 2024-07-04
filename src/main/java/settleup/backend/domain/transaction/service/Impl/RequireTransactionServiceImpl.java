package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
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
import settleup.backend.domain.transaction.repository.OptimizedTransactionDetailsRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.OptimizedDirectionService;
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
        processTransactionItems(requestDto);
    }

    private void processTransactionItems(TransactionDto requestDto) {
        List<ReceiptItemEntity> itemList = itemRepository.findByReceiptId(requestDto.getReceipt().getId());
        boolean allTransactionsSaved = true;

        for (ReceiptItemEntity item : itemList) {
            List<ReceiptItemUserEntity> itemUserList = itemUserRepository.findByReceiptItemId(item.getId());
            for (ReceiptItemUserEntity itemUser : itemUserList) {
                BigDecimal saveAmount = calculateSaveAmount(item, itemUser);
                if (isTransactionRequired(itemUser, requestDto) && !saveTransaction(itemUser, requestDto, saveAmount)) {
                    allTransactionsSaved = false;
                }
            }
        }

        if (allTransactionsSaved) {
            UserGroupDto userGroupDto = new UserGroupDto();
            userGroupDto.setGroup(requestDto.getGroup());
            optimizedDirectionService.performOptimizationOperations(userGroupDto);
        }
    }

    private void processEachTransaction(ReceiptItemEntity item, ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        BigDecimal saveAmount = calculateSaveAmount(item, itemUser);
        logger.debug("Processed transaction item with amount: {}, for user: {}", saveAmount, itemUser.getUser().getId());
        if (isTransactionRequired(itemUser, requestDto)) {
            saveTransaction(itemUser, requestDto, saveAmount);
        } else {
            logger.debug("Transaction not required for user: {}", itemUser.getUser().getId());
        }
    }

    private BigDecimal calculateSaveAmount(ReceiptItemEntity item, ReceiptItemUserEntity itemUser) {
        BigDecimal targetDividedValue = item.getUnitPrice().multiply(item.getItemQuantity());
        return Optional.ofNullable(itemUser.getPurchasedQuantity())
                .map(purchasedQuantity -> targetDividedValue.multiply(purchasedQuantity).divide(item.getItemQuantity(), BigDecimal.ROUND_HALF_UP))
                .orElse(targetDividedValue.divide(BigDecimal.valueOf(item.getJointPurchaserCount()), BigDecimal.ROUND_HALF_UP));
    }

    private boolean isTransactionRequired(ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        return !itemUser.getUser().getId().equals(requestDto.getReceipt().getPayerUser().getId());
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
        transaction.setTransactionUUID(uuidHelper.UUIDForTransaction());
        transaction.setReceipt(requestDto.getReceipt());
        transaction.setGroup(requestDto.getGroup());
        transaction.setRecipientUser(requestDto.getPayerUser());
        transaction.setRequiredReflection(Status.REQUIRE_OPTIMIZED);
        transaction.setSenderUser(itemUser.getUser());
        transaction.setTransactionAmount(saveAmount);
        return transaction;
    }
}

