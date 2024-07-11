package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.global.Helper.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedTransactionRepository extends JpaRepository<OptimizedTransactionEntity,Long> {
    @Modifying
    @Query("UPDATE OptimizedTransactionEntity o SET o.optimizationStatus = :status WHERE o.group = :group")
    void updateOptimizationStatusByGroup(@Param("group") AbstractGroupEntity group, @Param("status") Status status);

    @Modifying
    @Query("UPDATE OptimizedTransactionEntity o SET o.requiredReflection = :status WHERE o.transactionUUID = :uuid")
    void updateRequiredReflectionByTransactionUUID(@Param("uuid") String uuid, @Param("status") Status status);
    @Query("SELECT o.group FROM OptimizedTransactionEntity o WHERE o.id = :transactionId")
    AbstractGroupEntity findGroupByTransactionId(@Param("transactionId") Long transactionId);

    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND o.optimizationStatus = :currentStatus AND o.requiredReflection = :requireReflectStatus")
    List<TransactionalEntity> findTransactionsByGroupAndStatus(@Param("group") AbstractGroupEntity group, @Param("currentStatus") Status currentStatus, @Param("requireReflectStatus") Status requireReflectStatus);

    @Query("SELECT ot FROM OptimizedTransactionEntity ot " +
            "WHERE ot.group = :group " +
            "AND (ot.senderUser = :user OR ot.recipientUser = :user) " +
            "AND ot.optimizationStatus = 'CURRENT' " +
            "AND ot.hasBeenSent = false " +
            "AND ot.hasBeenChecked = false " +
            "AND ot.requiredReflection = 'REQUIRE_REFLECT'")
    List<TransactionalEntity> findFilteredTransactions(@Param("group") AbstractGroupEntity group,
                                                       @Param("user") AbstractUserEntity user);


    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.group = :group AND (o.senderUser = :user OR o.recipientUser = :user) AND o.hasBeenSent = true  AND o.clearStatusTimestamp >= :sevenDaysAgo")
    List<TransactionalEntity> findByGroupAndUserWithHAndHasBeenSentAndTransactionsSinceLastWeek(@Param("group") AbstractGroupEntity group, @Param("user") AbstractUserEntity user, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);


    Optional<OptimizedTransactionEntity> findByTransactionUUID(String uuid);



    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity o SET o.hasBeenSent = true WHERE o.transactionUUID = :uuid")
    void markHasBeenSentByUUID(@Param("uuid") String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity  o SET o.hasBeenChecked = true WHERE o.transactionUUID = :uuid")
    void markHasBeenCheckByUUID(@Param("uuid") String uuid);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity o SET o.requiredReflection = :requireReflectStatus WHERE o.id = :id")
    void updateRequiredReflectionStatusById(@Param("id") Long id, @Param("requireReflectStatus") Status requireReflectStatus);



    @Query("SELECT o FROM OptimizedTransactionEntity o WHERE o.recipientUser.id = :userId AND o.hasBeenSent = true AND o.hasBeenChecked = false AND o.group.id = :groupId")
    List<TransactionalEntity> findTransactionsForRecipientUserWithSentNotChecked(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("UPDATE OptimizedTransactionEntity o SET o.clearStatusTimestamp = :clearStatusTimestamp WHERE o.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);


}


