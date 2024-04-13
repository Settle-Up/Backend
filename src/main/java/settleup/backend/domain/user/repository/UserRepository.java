package settleup.backend.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByUserEmail(String email);

    Optional<UserEntity> findByUserUUID(String UUID);
    @Query("SELECT u FROM UserEntity u WHERE u.userEmail LIKE CONCAT('%', :partOfEmail, '%') AND u.userEmail != :notContainUserEmail")
    Page<UserEntity> findByUserEmailContainingAndUserEmailNot(@Param("partOfEmail") String partOfEmail, @Param("notContainUserEmail") String notContainUserEmail, Pageable pageable);


    @Query("SELECT u FROM UserEntity u WHERE u.userEmail LIKE %:email% AND u.id NOT IN :excludedUserIds")
    Page<UserEntity> findByEmailExcludingUsers(
            @Param("email") String email,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable);
}

