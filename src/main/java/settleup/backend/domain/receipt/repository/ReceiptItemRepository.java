package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;

@Repository
public interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity,Long> {
}
