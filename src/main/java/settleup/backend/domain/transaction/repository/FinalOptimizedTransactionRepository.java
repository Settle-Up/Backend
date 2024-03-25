package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.util.List;

@Repository
public interface FinalOptimizedTransactionRepository extends JpaRepository<FinalOptimizedTransactionEntity,Long> {
    @Modifying
    @Query("UPDATE FinalOptimizedTransactionEntity f SET f.isUsed = :status WHERE f.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND (f.senderUser = :user OR f.recipientUser = :user) AND f.isUsed = 'NOT_USED' AND f.isCleared <> 'CLEAR'")
    List<FinalOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND f.isUsed = 'NOT_USED'")
    List<FinalOptimizedTransactionEntity> findNotUsedTransactionsByGroup(@Param("group") GroupEntity group);

}
