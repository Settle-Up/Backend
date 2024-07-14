package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.global.Helper.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequireTransactionRepository extends JpaRepository<RequiresTransactionEntity, Long> {

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND r.requiredReflection= 'REQUIRE_OPTIMIZED'")
    List<RequiresTransactionEntity> findByGroupIdAndRequiredReflection(@Param("groupId") Long groupId);


    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND (r.senderUser.id = :userId OR r.recipientUser.id = :userId) AND (r.requiredReflection <> 'INHERITED_CLEAR')")
    List<RequiresTransactionEntity> findActiveTransactionsByGroupAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId);


    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId  AND (r.requiredReflection <> 'INHERITED_CLEAR')")
    List<RequiresTransactionEntity> findActiveTransactionsByGroup(@Param("groupId") Long groupId);

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.receipt.id = :receiptId")
    List<RequiresTransactionEntity> findByReceiptId(@Param("receiptId") Long receiptId);


    @Modifying
    @Query("UPDATE RequiresTransactionEntity rte SET rte.requiredReflection = :status WHERE rte.id = :id")
    void updateRequiredReflectionStatusById(@Param("id") Long id, @Param("status") Status status);

    @Modifying
    @Query("UPDATE RequiresTransactionEntity rte SET rte.clearStatusTimestamp = :timestamp WHERE rte.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);

    void deleteBySenderUserId(Long userId);
    void deleteByRecipientUserId(Long userId);


}