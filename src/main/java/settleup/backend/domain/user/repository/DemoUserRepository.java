package settleup.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.user.entity.DemoUserEntity;

public interface DemoUserRepository extends JpaRepository<DemoUserEntity ,Long> {
}
