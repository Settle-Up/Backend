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
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinalOptimizedTransactionRepository extends JpaRepository<FinalOptimizedTransactionEntity, Long> {
    @Modifying
    @Query("UPDATE FinalOptimizedTransactionEntity f SET f.isUsed = :status WHERE f.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND (f.senderUser = :user OR f.recipientUser = :user) AND f.isUsed = 'NOT_USED' AND (f.isSenderStatus <> 'CLEAR' OR f.isRecipientStatus <> 'CLEAR')")
    List<FinalOptimizedTransactionEntity> findByGroupAndUserAndStatusNotUsedAndNotCleared(@Param("group") GroupEntity group, @Param("user") UserEntity user);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND f.isUsed = 'NOT_USED'")
    List<FinalOptimizedTransactionEntity> findNotUsedTransactionsByGroup(@Param("group") GroupEntity group);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND (f.senderUser = :user OR f.recipientUser = :user) AND f.isSenderStatus = 'CLEAR' AND f.isRecipientStatus = 'CLEAR' AND f.clearStatusTimestamp >= :sevenDaysAgo")
    List<FinalOptimizedTransactionEntity> findByGroupAndUserWithStatusClearAndTransactionsSinceLastWeek(@Param("group") GroupEntity group, @Param("user") UserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
//    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND (f.senderUser = :user OR f.recipientUser = :user) AND f.isSenderStatus = 'CLEAR' AND f.isRecipientStatus = 'CLEAR' AND f.clearStatusTimestamp >= :sevenDaysAgo")
//    Page<FinalOptimizedTransactionEntity> findByGroupAndUserWithStatusClearAndTransactionsSinceLastWeek(GroupEntity group, UserEntity user, LocalDateTime startDate, Pageable pageable);

    Optional<FinalOptimizedTransactionEntity> findByTransactionUUID(String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE FinalOptimizedTransactionEntity f SET f.isSenderStatus = :status WHERE f.transactionUUID = :uuid")
    void updateIsSenderStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("UPDATE FinalOptimizedTransactionEntity f SET f.isRecipientStatus = :status WHERE f.transactionUUID = :uuid")
    void updateIsRecipientStatusByUUID(@Param("uuid") String uuid, @Param("status") Status status);

    @Query("SELECT f FROM FinalOptimizedTransactionEntity f WHERE f.group = :group AND " +
            "((f.isSenderStatus = 'CLEAR' AND f.isRecipientStatus <> 'CLEAR') OR " +
            "(f.isSenderStatus <> 'CLEAR' AND f.isRecipientStatus = 'CLEAR'))")
    List<FinalOptimizedTransactionEntity> findTransactionsWithOneSideClear(@Param("group") GroupEntity group);

    @Modifying
    @Transactional
    @Query("UPDATE FinalOptimizedTransactionEntity f SET f.clearStatusTimestamp = :clearStatusTimestamp WHERE f.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);
}