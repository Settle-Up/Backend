package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.global.exception.CustomException;

public interface TransactionSagaService {
    void performOptimizationOperations(TransactionDto transactionDto) throws CustomException;
}
