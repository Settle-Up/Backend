package settleup.backend.domain.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.FinalOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    Optional<OptimizedTransactionEntity> findByTransactionUUID(String uuid);

    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isUsed = 'NOT_USED' AND (o.isSenderStatus <> 'CLEAR' OR o.isRecipientStatus <> 'CLEAR') AND o.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<OptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);


    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isUsed = 'NOT_USED' AND (o.isSenderStatus <> 'CLEAR' OR o.isRecipientStatus <> 'CLEAR') AND o.isInheritanceStatus <> 'INHERITED_CLEAR' AND o.id NOT IN :excludedTransactionIds")
    List<OptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingTransactions(
            @Param("group") GroupEntity group,
            @Param("user") UserEntity user,
            @Param("excludedTransactionIds") List<Long> excludedTransactionIds);

    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isSenderStatus = 'CLEAR' AND o.isRecipientStatus = 'CLEAR' AND o.clearStatusTimestamp >= :sevenDaysAgo")
    List<OptimizedTransactionEntity> findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);


//    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.isSenderStatus = 'CLEAR' AND o.isRecipientStatus = 'CLEAR' AND o.clearStatusTimestamp >= :sevenDaysAgo")
//    Page<OptimizedTransactionEntity> findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity o SET o.isSenderStatus = :status WHERE o.transactionUUID = :uuid")
    void updateIsSenderStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity  o SET o.isRecipientStatus = :status WHERE o.transactionUUID = :uuid")
    void updateIsRecipientStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity o SET o.isInheritanceStatus = 'INHERITED_CLEAR' WHERE o.id = :id")
    void updateInheritanceStatusToClearById(@Param("id") Long id);

}
