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
public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionEntity,Long> {

    @Modifying
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.isUsed = :status WHERE g.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);

    List<GroupOptimizedTransactionEntity> findByGroupAndIsUsed(GroupEntity group, Status status);

    Optional<GroupOptimizedTransactionEntity> findByTransactionUUID(String uuid);


    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isUsed = 'NOT_USED' AND (g.isSenderStatus <> 'CLEAR' OR g.isRecipientStatus <> 'CLEAR') AND g.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);


    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isUsed = 'NOT_USED' AND (g.isSenderStatus <> 'CLEAR' OR g.isRecipientStatus <> 'CLEAR') AND g.isInheritanceStatus <> 'INHERITED_CLEAR' AND g.transactionUUID NOT IN :excludedUUIDs")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotClearedExcludingUUIDs(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("excludedUUIDs") List<String> excludedUUIDs);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND g.isUsed = 'NOT_USED'")
    List<GroupOptimizedTransactionEntity> findNotUsedTransactionsByGroup(@Param("group") GroupEntity group);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isSenderStatus = 'CLEAR' AND g.isRecipientStatus = 'CLEAR' AND g.clearStatusTimestamp >= :sevenDaysAgo")
    List<GroupOptimizedTransactionEntity> findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

//    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.isSenderStatus = 'CLEAR' AND g.isRecipientStatus = 'CLEAR' AND g.clearStatusTimestamp >= :sevenDaysAgo")
//    Page<GroupOptimizedTransactionEntity> findByGroupAndUserWithClearStatusAndTransactionsSinceLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,Pageable pageable);


    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.isSenderStatus = :status WHERE g.transactionUUID = :uuid")
    void updateIsSenderStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity  g SET g.isRecipientStatus = :status WHERE g.transactionUUID = :uuid")
    void updateIsRecipientStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.isInheritanceStatus = 'INHERITED_CLEAR' WHERE g.id = :id")
    void updateInheritanceStatusToClearById(@Param("id") Long id);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity  g WHERE g.group = :group AND " +
            "((g.isSenderStatus = 'CLEAR' AND g.isRecipientStatus <> 'CLEAR') OR " +
            "(g.isSenderStatus <> 'CLEAR' AND g.isRecipientStatus = 'CLEAR')) AND " +
            "g.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<GroupOptimizedTransactionEntity> findTransactionsWithOneSideClearAndNotInheritedClear(@Param("group") GroupEntity group);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity  g SET g.clearStatusTimestamp = :clearStatusTimestamp WHERE g.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);
}

