package settleup.backend.domain.group.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoGroupUserRepository extends GroupUserBaseRepository<DemoGroupUserEntity> {

    void deleteByGroupId(Long groupId);
    @Modifying
    @Transactional
    @Query("DELETE FROM DemoGroupUserEntity gu WHERE gu.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}


