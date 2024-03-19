package settleup.backend.domain.transaction.repository;

import com.sun.jdi.LongValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequireTransactionRepository extends JpaRepository<RequiresTransactionEntity, Long> {
    List<RequiresTransactionEntity> findBySenderUser_IdAndRecipientUser_Id(Long senderUserId, Long recipientUserId);
    List<RequiresTransactionEntity> findByRecipientUser_IdAndSenderUser_Id(Long recipientUserId, Long senderUserId);
}


