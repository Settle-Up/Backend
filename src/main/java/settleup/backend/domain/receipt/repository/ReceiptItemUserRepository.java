package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;

import java.util.List;

@Repository
public interface ReceiptItemUserRepository extends JpaRepository<ReceiptItemUserEntity,Long> {
    List<ReceiptItemUserEntity> findByReceiptItemId(Long id);
}
