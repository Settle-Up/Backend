package settleup.backend.domain.transaction.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;

public interface TransactionCoordinatorService {
    ResponseDto createExpenseByReceipt(ReceiptDto requestDto) throws CustomException;
}
