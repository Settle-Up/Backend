package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.global.Helper.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionEntity,Long> {

    @Modifying
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.optimizationStatus = :status WHERE g.group = :group")
    void updateOptimizationStatusByGroup(@Param("group") AbstractGroupEntity group, @Param("status") Status status);

    @Modifying
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.requiredReflection = :status WHERE g.transactionUUID = :uuid")
    void updateRequiredReflectionByTransactionUUID(@Param("uuid") String uuid, @Param("status") Status status);


    @Query("SELECT got FROM GroupOptimizedTransactionEntity got " +
            "WHERE got.group = :group " +
            "AND (got.senderUser = :user OR got.recipientUser = :user) " +
            "AND got.optimizationStatus = 'CURRENT' " +
            "AND got.hasBeenSent = false " +
            "AND got.hasBeenChecked = false " +
            "AND got.requiredReflection = 'REQUIRE_REFLECT'")
    List<TransactionalEntity> findFilteredTransactions(@Param("group") AbstractGroupEntity group,
                                                       @Param("user") AbstractUserEntity user);

    List<TransactionalEntity> findByGroupAndOptimizationStatus(AbstractGroupEntity group, Status status);

    Optional<GroupOptimizedTransactionEntity> findByTransactionUUID(String uuid);

    @Query("SELECT g FROM GroupOptimizedTransactionEntity g WHERE g.group = :group AND (g.senderUser = :user OR g.recipientUser = :user) AND g.hasBeenSent = true  AND g.clearStatusTimestamp >= :sevenDaysAgo")
    List<TransactionalEntity> findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(@Param("group") AbstractGroupEntity group, @Param("user") AbstractUserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);


    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.hasBeenSent = true WHERE g.transactionUUID = :uuid")
    void markHasBeenSentByUUID(@Param("uuid") String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity  g SET g.hasBeenChecked = true WHERE g.transactionUUID = :uuid")
    void markHasBeenCheckByUUID(@Param("uuid") String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.requiredReflection = :requireReflectStatus WHERE g.id = :id")
    void updateRequiredReflectionStatusById(@Param("id") Long id, @Param("requireReflectStatus") Status requireReflectStatus);




    @Query("SELECT g FROM GroupOptimizedTransactionEntity  g WHERE g.recipientUser.id = :userId AND g.hasBeenSent = true AND g.hasBeenChecked = false AND g.group.id = :groupId")
    List<TransactionalEntity> findTransactionsForRecipientUserWithSentNotChecked(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("UPDATE GroupOptimizedTransactionEntity  g SET g.clearStatusTimestamp = :clearStatusTimestamp WHERE g.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);

    void deleteBySenderUserId(Long userId);
    void deleteByRecipientUserId(Long userId);
}

