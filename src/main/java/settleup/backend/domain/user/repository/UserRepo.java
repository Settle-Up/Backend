package settleup.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.user.entity.UserEntity;
@Repository
public interface UserRepo extends JpaRepository<UserEntity ,Long> {
}
