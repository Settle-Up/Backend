package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptEntity,Long> {
    @Query("SELECT r FROM ReceiptEntity r WHERE r.receiptUUID = :receiptUUID")
    Optional<ReceiptEntity> findByReceiptUUID(String receiptUUID);

    @Query("SELECT ri FROM ReceiptItemEntity ri WHERE ri.receipt.receiptUUID = :receiptUUID")
    List<ReceiptItemEntity> findItemsByReceiptUUID(String receiptUUID);
    @Query("SELECT r FROM ReceiptEntity r WHERE r.group.id = :groupId ORDER BY r.createdAt DESC")
    List<ReceiptEntity> findReceiptByGroupId(Long groupId);

}
