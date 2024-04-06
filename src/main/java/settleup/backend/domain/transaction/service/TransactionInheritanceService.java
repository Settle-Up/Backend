package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;

import java.util.Optional;

public interface TransactionInheritanceService {
    void clearInheritanceStatusForOptimizedToRequired(Long optimizedTransactionId);

    void clearInheritanceStatusForGroupToOptimized(Long optimizedTransactionId);

    void clearInheritanceStatusForFinalToGroup(Optional<GroupOptimizedTransactionEntity> groupOptimizedTransaction);

    void clearInheritanceStatusForFinalToOptimized(Optional<OptimizedTransactionEntity> optimizedTransaction);
}
