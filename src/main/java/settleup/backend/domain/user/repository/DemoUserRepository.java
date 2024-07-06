package settleup.backend.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.user.entity.DemoUserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoUserRepository extends UserBaseRepository<DemoUserEntity> {
}
//    Optional<DemoUserEntity> findById(Long id);
//
//    Optional<DemoUserEntity> findByUserEmail(String email);
//
//    Optional<DemoUserEntity> findByUserUUID(String demoUserUUID);
//
//    @Query("SELECT u FROM DemoUserEntity u WHERE u.userEmail LIKE %:partOfEmail% AND u.userEmail <> :notContainUserEmail")
//    Page<DemoUserEntity> findByUserEmailContainingAndUserEmailNot(
//            @Param("partOfEmail") String partOfEmail,
//            @Param("notContainUserEmail") String notContainUserEmail,
//            Pageable pageable
//    );
//
//    @Query("SELECT u FROM DemoUserEntity u WHERE u.userEmail LIKE %:email% AND u.id NOT IN :excludedUserIds")
//    Page<DemoUserEntity> findByEmailExcludingUsers(
//            @Param("email") String email,
//            @Param("excludedUserIds") List<Long> excludedUserIds,
//            Pageable pageable
//    );
//}
