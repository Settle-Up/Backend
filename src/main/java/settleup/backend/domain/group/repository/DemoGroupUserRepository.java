package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;

public interface DemoGroupUserRepository extends JpaRepository<DemoGroupUserEntity,Long> {

}
