package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;

import java.util.List;
import java.util.Optional;

//@NoRepositoryBean
//public interface GroupUserBaseRepository<T extends GroupUserTypeEntity> extends JpaRepository<T, Long> {
//    List<T> findByGroup_Id(Long id);
//
//    List<T> findByUser_Id(Long userId);
//
//    @Query("SELECT gue FROM #{#entityName} gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
//    Optional<T> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
//
//    @Query("SELECT COUNT(gue) > 0 FROM #{#entityName} gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
//    boolean existsByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
//
//    @Query("SELECT CASE WHEN COUNT(gue) > 0 THEN true ELSE false END FROM #{#entityName} gue WHERE gue.user.id = :userId")
//    boolean existsByUserId(@Param("userId") Long userId);
//
//    void delete(GroupUserTypeEntity groupUser);
//}

@NoRepositoryBean
public interface GroupUserBaseRepository<T extends AbstractGroupUserEntity> extends JpaRepository<T, Long> {
    List<AbstractGroupUserEntity> findByGroup_Id(Long id);

    List<AbstractGroupUserEntity> findByUser_Id(Long userId);

    // 동적 엔티티 이름을 사용하여 유저와 그룹 ID로 그룹 유저 엔티티를 조회
    @Query("SELECT gue FROM #{#entityName} gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    Optional<AbstractGroupUserEntity> findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    // 동적 엔티티 이름을 사용하여 특정 유저 ID와 그룹 ID가 존재하는지 확인
    @Query("SELECT COUNT(gue) > 0 FROM #{#entityName} gue WHERE gue.user.id = :userId AND gue.group.id = :groupId")
    boolean existsByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    // 동적 엔티티 이름을 사용하여 특정 유저 ID가 존재하는지 확인
    @Query("SELECT CASE WHEN COUNT(gue) > 0 THEN true ELSE false END FROM #{#entityName} gue WHERE gue.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    void delete(AbstractGroupUserEntity groupUser);
}
