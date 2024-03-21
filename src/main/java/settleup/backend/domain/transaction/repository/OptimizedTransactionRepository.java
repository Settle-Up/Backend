package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.global.common.Status;

@Repository
public interface OptimizedTransactionRepository extends JpaRepository<OptimizedTransactionEntity,Long> {
    @Modifying
    @Query("UPDATE OptimizedTransactionEntity o SET o.isUsed = :status WHERE o.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);
    @Query("SELECT o.group FROM OptimizedTransactionEntity o WHERE o.id = :transactionId")
    GroupEntity findGroupByTransactionId(@Param("transactionId") Long transactionId);
}
