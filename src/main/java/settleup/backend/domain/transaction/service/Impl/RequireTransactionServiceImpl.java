package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
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
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class RequireTransactionServiceImpl implements RequireTransactionService {

    private final UUID_Helper uuidHelper;
    private final ReceiptItemRepository itemRepository;
    private final ReceiptItemUserRepository itemUserRepository;

    private final RequireTransactionRepository transactionRepository;

    /**
     * createExpense
     *
     * @param requestDto (receipt , group , allocationType , payerUser)
     * @return TransactionDto
     * @throws CustomException
     */


    @Override
    @Transactional
    public TransactionDto createExpense(TransactionDto requestDto) {
        processTransactionItems(requestDto);
        return requestDto;
    }


    private void processTransactionItems(TransactionDto requestDto) {
        List<ReceiptItemEntity> itemList = itemRepository.findByReceiptId(requestDto.getReceipt().getId());

        itemList.forEach(item -> {
            List<ReceiptItemUserEntity> itemUserList = itemUserRepository.findByReceiptItemId(item.getId());
            itemUserList.forEach(itemUser -> processEachTransaction(item, itemUser, requestDto));
        });
    }

    private void processEachTransaction(ReceiptItemEntity item, ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        double saveAmount = calculateSaveAmount(item, itemUser);
        if (isTransactionRequired(itemUser, requestDto)) {
            saveTransaction(itemUser, requestDto, saveAmount);
        }
    }

    private double calculateSaveAmount(ReceiptItemEntity item, ReceiptItemUserEntity itemUser) {
        double targetDividedValue = (float) (item.getUnitPrice() * item.getItemQuantity());
        return Optional.ofNullable(itemUser.getPurchasedQuantity())
                .map(purchasedQuantity -> targetDividedValue * purchasedQuantity / item.getItemQuantity())
                .orElseGet(() -> targetDividedValue / item.getJointPurchaserCount());
    }

    private boolean isTransactionRequired(ReceiptItemUserEntity itemUser, TransactionDto requestDto) {
        return !itemUser.getUser().getId().equals(requestDto.getReceipt().getPayerUser().getId());
    }

    private void saveTransaction(ReceiptItemUserEntity itemUser, TransactionDto requestDto, double saveAmount) {
        RequiresTransactionEntity transaction = createTransactionEntity(itemUser, requestDto, saveAmount);
      transactionRepository.save(transaction);

    }


    private RequiresTransactionEntity createTransactionEntity(ReceiptItemUserEntity itemUser, TransactionDto requestDto, double saveAmount) {
        RequiresTransactionEntity transaction = new RequiresTransactionEntity();
        transaction.setTransactionUUID(uuidHelper.UUIDForTransaction());
        transaction.setReceipt(requestDto.getReceipt());
        transaction.setGroup(requestDto.getGroup());
        transaction.setRecipientUser(requestDto.getPayerUser());
        transaction.setIsRecipientStatus(Status.PENDING);
        transaction.setIsSenderStatus(Status.PENDING);
        transaction.setIsInheritanceStatus(Status.PENDING);
        transaction.setSenderUser(itemUser.getUser());
        transaction.setTransactionAmount(saveAmount);
        return transaction;
    }
}