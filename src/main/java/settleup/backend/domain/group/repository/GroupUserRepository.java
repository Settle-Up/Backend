package settleup.backend.domain.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUserEntity, Long> {
    List<GroupUserEntity> findByGroup_Id(Long id);

    List<GroupUserEntity> findByUser_Id(Long userId);


    @Query(value = "SELECT gue.* FROM settle_group_user gue " +
            "JOIN settle_group g ON gue.group_id = g.id " +
            "LEFT JOIN (SELECT group_id, MAX(created_at) AS last_active FROM receipt GROUP BY group_id) r ON g.id = r.group_id " +
            "WHERE gue.user_id = :userId " +
            "ORDER BY CASE WHEN r.last_active IS NULL THEN 1 ELSE 0 END, COALESCE(r.last_active, g.created_at) DESC",
            nativeQuery = true)
    Page<GroupUserEntity> findByUserIdWithLatestReceiptOrCreatedAt(@Param("userId") Long userId, Pageable pageable);

    boolean existsByUser_Id(Long userId);

    @Query("SELECT gue FROM GroupUserEntity gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    Optional<GroupUserEntity> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Query("SELECT COUNT(gue) > 0 FROM GroupUserEntity gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    boolean existsByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

}



