package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
@Repository
public interface OptimizedTransactionDetailsRepository extends JpaRepository<OptimizedTransactionDetailsEntity,Long> {

}
