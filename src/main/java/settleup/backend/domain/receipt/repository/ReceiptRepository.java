package settleup.backend.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptEntity,Long> {
    Optional<ReceiptEntity> findByReceiptUUID(String UUID);
}
