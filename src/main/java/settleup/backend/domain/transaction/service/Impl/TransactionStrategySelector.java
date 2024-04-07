package settleup.backend.domain.transaction.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.repository.FinalOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionDetailRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.global.common.Status;
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
    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
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

    @Transactional
    public String selectRepository(String transactionId, TransactionUpdateRequestDto request) throws CustomException {
        Status statusToUpdate = Status.valueOf(request.getApprovalStatus());
        if (transactionId.startsWith("FPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                mergeOptimizedRepo.updateIsSenderStatusByUUID(transactionId, statusToUpdate);
            } else {
                mergeOptimizedRepo.updateIsRecipientStatusByUUID(transactionId, statusToUpdate);
            }
        } else if (transactionId.startsWith("GPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                groupOptimizedTransactionRepo.updateIsSenderStatusByUUID(transactionId, statusToUpdate);
            } else {
                groupOptimizedTransactionRepo.updateIsRecipientStatusByUUID(transactionId, statusToUpdate);
            }
        } else if (transactionId.startsWith("OPT")) {
            if ("sender".equals(request.getApprovalUser())) {
                optimizedTransactionRepo.updateIsSenderStatusByUUID(transactionId, statusToUpdate);
            } else {
                optimizedTransactionRepo.updateIsRecipientStatusByUUID(transactionId, statusToUpdate);
            }
        } else {
            throw new CustomException(ErrorCode.TRANSACTION_TYPE_NOT_SUPPORTED);
        }
        entityManager.flush();
        return "save-success";
    }

}