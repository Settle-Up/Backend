package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;

public interface OptimizedTransactionDetailsRepository extends JpaRepository<OptimizedTransactionDetailsEntity,Long> {
}
