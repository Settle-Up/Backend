package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUserEntity,Long> {
    List<GroupUserEntity> findByGroup_Id(Long id);

    List<GroupUserEntity> findByUser_Id(Long userId);

    @Query("SELECT gue.user.id FROM GroupUserEntity gue WHERE gue.group.groupUUID = :groupUUID")
    List<Long> findUserIdsByGroup_GroupUUID(@Param("groupUUID") String groupUUID);

    boolean existsByUser_Id(Long userId);

    @Query("SELECT gue FROM GroupUserEntity gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    Optional<GroupUserEntity> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}



