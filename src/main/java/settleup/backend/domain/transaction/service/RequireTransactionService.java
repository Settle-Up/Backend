package settleup.backend.domain.transaction.service;

import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.global.exception.CustomException;

import java.util.Map;
import java.util.Objects;

public interface RequireTransactionService {
    String  createExpense(RequireTransactionDto transactionDto) throws CustomException;
}
