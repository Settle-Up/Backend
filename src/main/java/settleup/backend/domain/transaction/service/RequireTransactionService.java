package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.global.exception.CustomException;

import java.util.concurrent.CompletableFuture;

public interface RequireTransactionService {
    CompletableFuture<TransactionDto> createExpense(TransactionDto transactionDto) throws CustomException;
}
