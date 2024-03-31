package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.repository.FinalOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionDetailRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TransactionStrategySelector {
    private final GroupOptimizedService groupOptimizedService;
    private final OptimizedService optimizedService;
    private final FinalOptimizedService finalOptimizedService;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final FinalOptimizedTransactionRepository mergeOptimizedRepo;


    public TransactionProcessingService selectService(String transactionId) throws CustomException {
        if (transactionId.startsWith("FPT")) {
            return finalOptimizedService;
        } else if (transactionId.startsWith("GPT")) {
            return groupOptimizedService;
        } else if (transactionId.startsWith("OPT")) {
            return optimizedService;
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }
    }

    public TransactionalEntity selectRepository(String transactionId) throws CustomException {
        Optional<? extends TransactionalEntity> result;
        if (transactionId.startsWith("FPT")) {
            result = mergeOptimizedRepo.findByTransactionUUID(transactionId);
        } else if (transactionId.startsWith("GPT")) {
            result = groupOptimizedTransactionRepo.findByTransactionUUID(transactionId);
        } else if (transactionId.startsWith("OPT")) {
            result = optimizedTransactionRepo.findByTransactionUUID(transactionId);
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }

        return result.orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));
    }

}