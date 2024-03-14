package settleup.backend.domain.transaction.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;

public interface TransactionCoordinatorService {
    ResponseDto createExpenseByReceipt(ReceiptRequestDto requestDto) throws CustomException;
}
