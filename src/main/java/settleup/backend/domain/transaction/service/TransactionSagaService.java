package settleup.backend.domain.transaction.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;

import java.util.concurrent.CompletableFuture;

public interface TransactionSagaService {
    CompletableFuture<Void>performAsyncOperations(TransactionDto transactionDto) throws CustomException;
}
