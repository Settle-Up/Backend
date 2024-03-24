package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;

@Repository
public interface FinalOptimizedTransactionRepository extends JpaRepository<FinalOptimizedTransactionEntity,Long> {
}
