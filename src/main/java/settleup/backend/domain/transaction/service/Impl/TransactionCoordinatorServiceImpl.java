package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.transaction.service.TransactionCoordinatorService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;

import java.util.HashMap;
import java.util.Map;

@Transactional
@AllArgsConstructor
@Service
public class TransactionCoordinatorServiceImpl implements TransactionCoordinatorService {
    private final ReceiptService receiptService;
    private final RequireTransactionService requireTransactionService;
    private final ReceiptRepository receiptRepository;

    @Override
    public ResponseDto createExpenseByReceipt(ReceiptRequestDto requestDto) throws CustomException {
        RequireTransactionDto transactionDto = receiptService.createReceipt(requestDto);
        String transactionUUID =requireTransactionService.createExpense(transactionDto);
        Map<String, String> data = new HashMap<>();
        data.put("receiptId",transactionUUID);
        data.put("receiptName",receiptRepository.findByReceiptUUID(transactionUUID).get().getReceiptName());
        data.put("createAt", String.valueOf(receiptRepository.findByReceiptUUID(transactionUUID).get().getCreatedAt()));
        return new ResponseDto<>(true,"receipt , expense data successfully save",data);
    }
}
