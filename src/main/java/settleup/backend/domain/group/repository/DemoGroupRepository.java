package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.DemoGroupEntity;
import settleup.backend.domain.group.entity.GroupEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface DemoGroupRepository extends GroupBaseRepository<DemoGroupEntity> {

    @Query("SELECT g.id FROM DemoGroupEntity g WHERE g.createdAt < :expirationTime")
    List<Long> findExpiredGroupIds(@Param("expirationTime") LocalDateTime expirationTime);

    @Modifying
    @Query("DELETE FROM AbstractGroupEntity g WHERE g.id = :groupId")
    void deleteAbstractGroupById(@Param("groupId") Long groupId);
};





