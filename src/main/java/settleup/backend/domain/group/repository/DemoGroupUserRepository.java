package settleup.backend.domain.group.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoGroupUserRepository extends GroupUserBaseRepository<DemoGroupUserEntity> {
}
//        List<DemoGroupUserEntity> findByDemoGroup_Id(Long id);
//
//        List<DemoGroupUserEntity> findByDemoUser_Id(Long userId);
//
//        @Query(value = "SELECT gue.* FROM settle_demo_group_user gue " +
//                "JOIN settle_demo_group g ON gue.demo_group_id = g.id " +
//                "LEFT JOIN (SELECT demo_group_id, MAX(created_at) AS last_active FROM receipt GROUP BY demo_group_id) r ON g.id = r.demo_group_id " +
//                "WHERE gue.demo_user_id = :userId " +
//                "ORDER BY CASE WHEN r.last_active IS NULL THEN 1 ELSE 0 END, COALESCE(r.last_active, g.created_at) DESC",
//                nativeQuery = true)
//        Page<DemoGroupUserEntity> findByUserIdWithLatestReceiptOrCreatedAt(@Param("userId") Long userId, Pageable pageable);
//
//        boolean existsByDemoUser_Id(Long userId);
//
//        @Query("SELECT gue FROM DemoGroupUserEntity gue WHERE gue.User.id = :userId AND gue.Group.id = :groupId")
//        Optional<DemoGroupUserEntity> findByDemoUserIdAndDemoGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
//
//        @Query("SELECT COUNT(gue) > 0 FROM DemoGroupUserEntity gue WHERE gue.User.id = :userId AND gue.Group.id = :groupId")
//        boolean existsByDemoUserUserIdAndDemoGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
//}
