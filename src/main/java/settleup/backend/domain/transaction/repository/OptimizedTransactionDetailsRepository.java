package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.util.List;

@Repository
public interface OptimizedTransactionDetailsRepository extends JpaRepository<OptimizedTransactionDetailsEntity,Long> {

    @Query("SELECT d.requiresTransaction.id FROM OptimizedTransactionDetailsEntity d WHERE d.optimizedTransaction.id = :id")
    List<Long> findRequiresTransactionIdsByOptimizedTransactionId(@Param("id") Long id);


    @Query("SELECT d FROM OptimizedTransactionDetailsEntity d WHERE d.optimizedTransaction.id = :id")
    List<OptimizedTransactionDetailsEntity> findByOptimizedTransactionId(@Param("id") Long id);

}

