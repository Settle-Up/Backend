package settleup.backend.global.Selector;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.repository.UltimateOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Transactional
public class TransactionStrategySelector {
    private final GroupOptimizedService groupOptimizedService;
    private final OptimizedService optimizedService;
    private final UltimateOptimizedService ultimateOptimizedService;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final UltimateOptimizedTransactionRepository ultimateOptimizedRepo;
    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public TransactionProcessingService selectService(String transactionId) throws CustomException {
        if (transactionId.startsWith("FPT")) {
            return ultimateOptimizedService;
        } else if (transactionId.startsWith("GPT")) {
            return groupOptimizedService;
        } else if (transactionId.startsWith("OPT")) {
            return optimizedService;
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * request 안에 든거 => transactionId , approvalUser(sender or recipient)
     */

    @Transactional
    public String selectRepository(TransactionUpdateRequestDto request) throws CustomException {
        String transactionId = request.getTransactionId();
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        if (transactionId.startsWith("FPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                ultimateOptimizedRepo.markHasBeenSentByUUID(transactionId);
                ultimateOptimizedRepo.updateClearStatusTimestampById
                        (ultimateOptimizedRepo.findByTransactionUUID(transactionId).get().getId(), newClearStatusTimestamp);
            } else {
                ultimateOptimizedRepo.markHasBeenCheckByUUID(transactionId);
            }
        } else if (transactionId.startsWith("GPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                groupOptimizedTransactionRepo.markHasBeenSentByUUID(transactionId);
                groupOptimizedTransactionRepo.updateClearStatusTimestampById
                        (groupOptimizedTransactionRepo.findByTransactionUUID(transactionId).get().getId(), newClearStatusTimestamp);
            } else {
                groupOptimizedTransactionRepo.markHasBeenCheckByUUID(transactionId);
            }
        } else if (transactionId.startsWith("OPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                optimizedTransactionRepo.markHasBeenSentByUUID(transactionId);
                optimizedTransactionRepo.updateClearStatusTimestampById
                        (optimizedTransactionRepo.findByTransactionUUID(transactionId).get().getId(), newClearStatusTimestamp);
            } else {
                optimizedTransactionRepo.markHasBeenCheckByUUID(transactionId);
            }
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }
        entityManager.flush();
        return "save-success";
    }

}