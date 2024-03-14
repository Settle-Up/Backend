package settleup.backend.domain.transaction.service;

import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.global.exception.CustomException;

public interface RequireTransactionService {
    void createExpense(RequireTransactionDto transactionDto) throws CustomException;
}
