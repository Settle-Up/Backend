package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;

import java.util.List;

@Repository
public interface GroupOptimizedTransactionDetailRepository extends JpaRepository<GroupOptimizedTransactionDetailsEntity,Long> {
    @Query("SELECT d.optimizedTransaction FROM GroupOptimizedTransactionDetailsEntity d WHERE d.groupOptimizedTransaction IN :groupOptimizedTransactions")
    List<OptimizedTransactionEntity> findOptimizedTransactionsByGroupOptimizedTransactions(@Param("groupOptimizedTransactions") List<GroupOptimizedTransactionEntity> groupOptimizedTransactions);

    List<GroupOptimizedTransactionDetailsEntity> findByGroupOptimizedTransactionId(Long id);
}

