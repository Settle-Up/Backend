package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.UltimateOptimizedTransactionDetailEntity;

import java.util.List;

@Repository
public interface UltimateOptimizedTransactionDetailRepository extends JpaRepository<UltimateOptimizedTransactionDetailEntity,Long> {


    @Query("SELECT f.usedOptimizedTransaction FROM UltimateOptimizedTransactionDetailEntity f WHERE f.ultimateOptimizedTransaction.id = :ultimateOptimizedTransactionId")
    List<String> findUsedOptimizedTransactionUuidsByUltimateTransactionId(@Param("ultimateOptimizedTransactionId") Long ultimateOptimizedTransactionId);

    List<UltimateOptimizedTransactionDetailEntity> findByUltimateOptimizedTransactionId(Long id);
    void deleteByUltimateOptimizedTransaction_SenderUser_Id(Long userId);
    void deleteByUltimateOptimizedTransaction_RecipientUser_Id(Long userId);
}

