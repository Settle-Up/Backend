package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;

public interface OptimizedTransactionRepository extends JpaRepository<OptimizedTransactionEntity,Long> {
}
