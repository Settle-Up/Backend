package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;

public interface GroupOptimizedTransactionDetailRepository extends JpaRepository<GroupOptimizedTransactionDetailsEntity,Long> {
}
