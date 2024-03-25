package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionDetailEntity;

import java.util.List;

@Repository
public interface FinalOptimizedTransactionDetailRepository extends JpaRepository<FinalOptimizedTransactionDetailEntity,Long> {

    @Query("SELECT f.usedOptimizedTransaction FROM FinalOptimizedTransactionDetailEntity f WHERE f.finalOptimizedTransaction.id = :finalOptimizedTransactionId")
    List<String> findUsedOptimizedTransactionUuidsByFinalOptimizedTransactionId(@Param("finalOptimizedTransactionId") Long finalOptimizedTransactionId);
}

