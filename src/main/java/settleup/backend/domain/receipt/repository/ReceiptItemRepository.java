package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;

import java.util.List;

@Repository
public interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity,Long> {
    List<ReceiptItemEntity> findByReceiptId(Long id);

    @Query("SELECT ri.id FROM ReceiptItemEntity ri WHERE ri.receipt.id IN (SELECT r.id FROM ReceiptEntity r WHERE r.payerUser.id = :userId)")
    List<Long> findIdsByReceiptPayerUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReceiptItemEntity ri WHERE ri.receipt.id IN (SELECT r.id FROM ReceiptEntity r WHERE r.payerUser.id = :userId)")
    void deleteByReceiptPayerUserId(@Param("userId") Long userId);


    @Modifying
    @Query("DELETE FROM ReceiptItemEntity r WHERE r.receipt.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

}
