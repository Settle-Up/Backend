package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;

@Repository
public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionEntity,Long> {
}
