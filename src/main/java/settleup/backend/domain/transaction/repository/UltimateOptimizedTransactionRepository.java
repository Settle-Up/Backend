package settleup.backend.domain.transaction.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.transaction.model.TransactionalEntity;
import settleup.backend.domain.transaction.entity.UltimateOptimizedTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.global.Helper.Status;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UltimateOptimizedTransactionRepository extends JpaRepository<UltimateOptimizedTransactionEntity, Long> {
    @Modifying
    @Query("UPDATE UltimateOptimizedTransactionEntity f SET f.optimizationStatus = :status WHERE f.group = :group")
    void updateOptimizationStatusByGroup(@Param("group") GroupTypeEntity group, @Param("status") Status status);

    @Query("SELECT uot FROM UltimateOptimizedTransactionEntity uot " +
            "WHERE uot.group = :group " +
            "AND (uot.senderUser = :user OR uot.recipientUser = :user) " +
            "AND uot.optimizationStatus = 'CURRENT' " +
            "AND uot.hasBeenSent = false " +
            "AND uot.hasBeenChecked = false " +
            "AND uot.requiredReflection = 'REQUIRE_REFLECT'")
    List<TransactionalEntity> findFilteredTransactions(@Param("group") GroupTypeEntity group,
                                                       @Param("user") UserTypeEntity user);
    @Query("SELECT f FROM UltimateOptimizedTransactionEntity f WHERE f.group = :group AND (f.senderUser = :user OR f.recipientUser = :user) AND f.hasBeenSent = true  AND f.clearStatusTimestamp >= :sevenDaysAgo")
    List<TransactionalEntity> findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(@Param("group") GroupTypeEntity group, @Param("user") UserTypeEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);


    @Query("SELECT u FROM UltimateOptimizedTransactionEntity u WHERE u.group = :group AND u.optimizationStatus = :status")
    List<UltimateOptimizedTransactionEntity> findTransactionsByGroupAndStatus(@Param("group") GroupEntity group, @Param("status") Status status);


    Optional<UltimateOptimizedTransactionEntity> findByTransactionUUID(String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE UltimateOptimizedTransactionEntity uote SET uote.hasBeenSent = true WHERE uote.transactionUUID = :uuid")
    void markHasBeenSentByUUID(@Param("uuid") String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE UltimateOptimizedTransactionEntity uote SET uote.hasBeenChecked = true WHERE uote.transactionUUID = :uuid")
    void markHasBeenCheckByUUID(@Param("uuid") String uuid);

    @Query("SELECT u FROM UltimateOptimizedTransactionEntity u WHERE u.recipientUser.id = :userId AND u.hasBeenSent = true AND u.hasBeenChecked = false AND u.group.id = :groupId")
    List<TransactionalEntity> findTransactionsForRecipientUserWithSentNotChecked(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("UPDATE UltimateOptimizedTransactionEntity f SET f.clearStatusTimestamp = :clearStatusTimestamp WHERE f.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);
}