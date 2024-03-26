package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionEntity,Long> {

    @Modifying
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.isUsed = :status WHERE g.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);

    List<GroupOptimizedTransactionEntity> findByGroupAndIsUsed(GroupEntity group, Status status);

    GroupOptimizedTransactionEntity findByTransactionUUID(String uuid);


    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isUsed = 'NOT_USED' AND (g.isSenderStatus <> 'CLEAR' OR g.isRecipientStatus <> 'CLEAR') AND g.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);


    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isUsed = 'NOT_USED' AND (g.isSenderStatus <> 'CLEAR' OR g.isRecipientStatus <> 'CLEAR') AND g.isInheritanceStatus <> 'INHERITED_CLEAR' AND g.transactionUUID NOT IN :excludedUUIDs")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingUUIDs(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("excludedUUIDs") List<String> excludedUUIDs);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND g.isUsed = 'NOT_USED'")
    List<GroupOptimizedTransactionEntity> findNotUsedTransactionsByGroup(@Param("group") GroupEntity group);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.createdAt >= :startDate")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserAndTransactionsWithinLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("startDate") LocalDateTime startDate);

}
