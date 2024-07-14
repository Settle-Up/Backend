package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;

import java.util.List;

@Repository
public interface ReceiptItemUserRepository extends JpaRepository<ReceiptItemUserEntity,Long> {

    List<ReceiptItemUserEntity> findByReceiptItemId(Long id);


    @Modifying
    @Query("DELETE FROM ReceiptItemUserEntity r WHERE r.receiptItem.id IN :receiptItemIds")
    void deleteByReceiptItem_IdIn(@Param("receiptItemIds") List<Long> receiptItemIds);

    @Modifying
    @Query("DELETE FROM ReceiptItemUserEntity r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ReceiptItemUserEntity r WHERE r.receiptItem.receipt.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
