package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OptimizedTransactionRepository extends JpaRepository<OptimizedTransactionEntity,Long> {
    @Modifying
    @Query("UPDATE OptimizedTransactionEntity o SET o.isUsed = :status WHERE o.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);
    @Query("SELECT o.group FROM OptimizedTransactionEntity o WHERE o.id = :transactionId")
    GroupEntity findGroupByTransactionId(@Param("transactionId") Long transactionId);

    @Query("SELECT ot FROM OptimizedTransactionEntity ot " +
            "WHERE ot.group = :group " +
            "AND ot.isUsed = 'NOT_USED' " +
            "AND ot.id NOT IN (" +
            "SELECT god.optimizedTransaction.id FROM GroupOptimizedTransactionDetailsEntity god " +
            "WHERE god.groupOptimizedTransaction.id = :groupOptimizedTransactionId)")
    List<OptimizedTransactionEntity> findAvailableOptimizedTransactions(@Param("group") GroupEntity group,
                                                                        @Param("groupOptimizedTransactionId") Long groupOptimizedTransactionId);
    OptimizedTransactionEntity findByTransactionUUID(String uuid);

    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isUsed = 'NOT_USED' AND (o.isSenderStatus <> 'CLEAR' OR o.isRecipientStatus <> 'CLEAR') AND o.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<OptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);


    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isUsed = 'NOT_USED' AND (o.isSenderStatus <> 'CLEAR' OR o.isRecipientStatus <> 'CLEAR') AND o.isInheritanceStatus <> 'INHERITED_CLEAR' AND o.id NOT IN :excludedTransactionIds")
    List<OptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingTransactions(
            @Param("group") GroupEntity group,
            @Param("user") UserEntity user,
            @Param("excludedTransactionIds") List<Long> excludedTransactionIds);

    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.createdAt >= :startDate")
    List<OptimizedTransactionEntity> findByGroupAndUserAndTransactionsWithinLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("startDate") LocalDateTime startDate);

}
