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

    @Query("SELECT gue FROM GroupUserEntity gue JOIN FETCH gue.group g LEFT JOIN ReceiptEntity r ON g.id = r.group.id WHERE gue.user.id = :userId GROUP BY gue.id ORDER BY MAX(r.createdAt) DESC")
    Page<GroupUserEntity> findByUserIdWithLatestReceipt(Long userId, Pageable pageable);

    boolean existsByUser_Id(Long userId);

    @Query("SELECT gue FROM GroupUserEntity gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    Optional<GroupUserEntity> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}



