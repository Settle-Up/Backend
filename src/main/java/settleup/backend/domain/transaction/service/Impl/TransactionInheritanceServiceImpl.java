package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.*;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;
import settleup.backend.global.common.Status;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionInheritanceServiceImpl implements TransactionInheritanceService {
    private final RequireTransactionRepository requireTransactionRepo;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final OptimizedTransactionDetailsRepository optimizedTransactionDetailsRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedTransactionDetailRepo;
    private static final Logger log = LoggerFactory.getLogger(TransactionInheritanceServiceImpl.class);

    @Transactional
    public void clearInheritanceStatusFromOptimizedToRequired(Long requireTransactionId) {
            requireTransactionRepo.updateRequiredReflectionStatusById(requireTransactionId, Status.INHERITED_CLEAR);
            LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
            requireTransactionRepo.updateClearStatusTimestampById(requireTransactionId, newClearStatusTimestamp);

    }



    @Transactional
    public void clearInheritanceStatusFromGroupToOptimized(Long optimizedTransactionId) {
        optimizedTransactionRepo.updateRequiredReflectionStatusById(optimizedTransactionId, Status.INHERITED_CLEAR);
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        optimizedTransactionRepo.updateClearStatusTimestampById(optimizedTransactionId, newClearStatusTimestamp);
        clearInheritanceStatusFromOptimizedToRequired(optimizedTransactionId);
    }


    @Transactional
    public void clearInheritanceStatusFromUltimateToGroup(Optional<GroupOptimizedTransactionEntity> groupOptimizedTransaction) {
        groupOptimizedTransactionRepo.updateRequiredReflectionStatusById(groupOptimizedTransaction.get().getId(),Status.INHERITED_CLEAR);
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        groupOptimizedTransactionRepo.updateClearStatusTimestampById(groupOptimizedTransaction.get().getId(), newClearStatusTimestamp);

        List<GroupOptimizedTransactionDetailsEntity> goesFromGroupToOptimizedList =
                groupOptimizedTransactionDetailRepo.findByGroupOptimizedTransactionId(groupOptimizedTransaction.get().getId());
        for (GroupOptimizedTransactionDetailsEntity goesToGroupToOptimized : goesFromGroupToOptimizedList) {
            clearInheritanceStatusFromGroupToOptimized(goesToGroupToOptimized.getOptimizedTransaction().getId());
        }
    }

    @Override
    @Transactional
    public void clearInheritanceStatusFromUltimateToOptimized(Optional<OptimizedTransactionEntity> optimizedTransaction) {
        optimizedTransactionRepo.updateRequiredReflectionStatusById(optimizedTransaction.get().getId(),Status.INHERITED_CLEAR);
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        optimizedTransactionRepo.updateClearStatusTimestampById(optimizedTransaction.get().getId(), newClearStatusTimestamp);
        clearInheritanceStatusFromOptimizedToRequired(optimizedTransaction.get().getId());
    }
}