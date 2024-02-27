package settleup.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity ,Long> {
    Optional<UserEntity> findByUserEmail(String email);
    Optional<UserEntity> findByUserUUID(String UUID);

}
