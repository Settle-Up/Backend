package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class RequireTransactionServiceImpl implements RequireTransactionService {

    private final UUID_Helper uuidHelper;
    private final ReceiptItemRepository itemRepository;
    private final ReceiptItemUserRepository itemUserRepository;
    private final UserRepository userRepository;
    private final RequireTransactionRepository transactionRepository;


    @Transactional
    @Override
    public String createExpense(RequireTransactionDto transactionDto) throws CustomException {

        Long receiptId = transactionDto.getReceipt().getId();
        List<ReceiptItemEntity> itemList = itemRepository.findByReceiptId(receiptId);
        double totalSaveAmount = 0;

        for (ReceiptItemEntity item : itemList) {
            double itemPrice = item.getItemPrice();
            double totalItemQuantity = item.getItemQuantity();
            double engageCount = item.getEngagerCount();
            Float distributeValue = (float) (itemPrice * totalItemQuantity);

            List<ReceiptItemUserEntity> itemUserList = itemUserRepository.findByReceiptItemId(item.getId());
            for (ReceiptItemUserEntity itemUser : itemUserList) {
                double saveAmount;
                if (itemUser.getPurchasedQuantity() != null) {
                    double itemQuantityEachPerson = itemUser.getPurchasedQuantity();
                    saveAmount = (distributeValue * itemQuantityEachPerson / totalItemQuantity);
                } else {
                    saveAmount = distributeValue / engageCount;
                }
                totalSaveAmount += saveAmount;
                if (!itemUser.getUser().getId().equals(transactionDto.getReceipt().getPayerUser().getId())) {
                    RequiresTransactionEntity transaction = new RequiresTransactionEntity();
                    String transactionUUID = uuidHelper.UUIDForTransaction();
                    transaction.setTransactionUUID(transactionUUID);
                    transaction.setReceipt(transactionDto.getReceipt());
                    transaction.setGroup(transactionDto.getGroup());
                    transaction.setRecipientUser(transactionDto.getPayerUser());
                    transaction.setIsRecipientStatus(Status.PENDING);
                    transaction.setIsSenderStatus(Status.PENDING);
                    transaction.setSenderUser(userRepository.findById(itemUser.getUser().getId()).get());
                    transaction.setTransactionAmount(Double.valueOf(saveAmount));
                    transactionRepository.save(transaction);
                }
            }
        }
        if (totalSaveAmount != transactionDto.getReceipt().getActualPaidPrice()) {
            throw new CustomException(ErrorCode.TOTAL_AMOUNT_ERROR);
        }

        return transactionDto.getReceipt().getReceiptUUID();

    }
}
