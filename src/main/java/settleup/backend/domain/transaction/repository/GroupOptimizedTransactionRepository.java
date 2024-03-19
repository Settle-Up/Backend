package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;

public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionDetailsEntity,Long> {
}
