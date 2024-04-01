package settleup.backend.domain.transaction.repository;

import com.sun.jdi.LongValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequireTransactionRepository extends JpaRepository<RequiresTransactionEntity, Long> {

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND (r.isSenderStatus <> 'CLEAR' OR r.isRecipientStatus <> 'CLEAR') AND r.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<RequiresTransactionEntity> findByGroupIdAndStatusNotClearAndNotInherited(@Param("groupId") Long groupId);

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND (r.senderUser.id = :userId OR r.recipientUser.id = :userId) AND (r.isSenderStatus <> 'CLEAR' OR r.isRecipientStatus <> 'CLEAR') AND r.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<RequiresTransactionEntity> findByGroupAndUserAndStatusNotClearAndNotInherited(@Param("groupId") Long groupId, @Param("userId") Long userId);


    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND r.id <> :id")
    List<RequiresTransactionEntity> findAllByGroupIdExcludingId(@Param("groupId") Long groupId, @Param("id") Long id);

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.receipt.id = :receiptId")
    List<RequiresTransactionEntity> findByReceiptId(@Param("receiptId") Long receiptId);

    @Modifying
    @Transactional
    @Query("UPDATE RequiresTransactionEntity r SET r.isInheritanceStatus = 'INHERITED_CLEAR' WHERE r.id = :id")
    void updateInheritanceStatusToClearById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE RequiresTransactionEntity r SET r.clearStatusTimestamp = :clearStatusTimestamp WHERE r.id = :id")
    void updateClearStatusTimestampById(@Param("id") Long id, @Param("clearStatusTimestamp") LocalDateTime clearStatusTimestamp);
}



