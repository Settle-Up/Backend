package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;

import java.util.Optional;

public interface TransactionInheritanceService {
    void clearInheritanceStatusFromOptimizedToRequired(Long optimizedTransactionId);

    void clearInheritanceStatusFromGroupToOptimized(Long optimizedTransactionId);

    void clearInheritanceStatusFromUltimateToGroup(Optional<GroupOptimizedTransactionEntity> groupOptimizedTransaction);

    void clearInheritanceStatusFromUltimateToOptimized(Optional<OptimizedTransactionEntity> optimizedTransaction);
}
