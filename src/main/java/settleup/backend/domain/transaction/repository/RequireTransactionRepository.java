package settleup.backend.domain.transaction.repository;

import com.sun.jdi.LongValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequireTransactionRepository extends JpaRepository<RequiresTransactionEntity, Long> {

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND (r.isSenderStatus <> 'CLEAR' OR r.isRecipientStatus <> 'CLEAR') AND r.isInheritanceStatus <> 'INHERITED_CLEAR'")
    List<RequiresTransactionEntity> findByGroupIdAndStatusNotClearAndNotInherited(@Param("groupId") Long groupId);

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.group.id = :groupId AND r.id <> :id")
    List<RequiresTransactionEntity> findAllByGroupIdExcludingId(@Param("groupId") Long groupId, @Param("id") Long id);

    @Query("SELECT r FROM RequiresTransactionEntity r WHERE r.receipt.id = :receiptId")
    List<RequiresTransactionEntity> findByReceiptId(@Param("receiptId") Long receiptId);

}



