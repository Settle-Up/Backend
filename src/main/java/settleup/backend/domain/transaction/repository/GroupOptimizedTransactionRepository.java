package settleup.backend.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.global.common.Status;

import java.util.List;

@Repository
public interface GroupOptimizedTransactionRepository extends JpaRepository<GroupOptimizedTransactionEntity,Long> {

    @Modifying
    @Query("UPDATE GroupOptimizedTransactionEntity g SET g.isUsed = :status WHERE g.group = :group")
    void updateIsUsedStatusByGroup(@Param("group") GroupEntity group, @Param("status") Status status);

    List<GroupOptimizedTransactionEntity> findByGroupAndIsUsed(GroupEntity group, Status status);
    GroupOptimizedTransactionEntity findByGroupOptimizedTransactionUUID(String uuid);
    }
