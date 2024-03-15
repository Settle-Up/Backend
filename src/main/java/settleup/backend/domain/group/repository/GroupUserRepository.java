package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUserEntity,Long> {
    List<GroupUserEntity> findByGroup_Id(Long id);
    List<GroupUserEntity> findByUserId(Long userId);


}
