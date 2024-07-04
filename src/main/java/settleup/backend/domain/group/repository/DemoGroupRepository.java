package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import settleup.backend.domain.group.entity.DemoGroupEntity;

public interface DemoGroupRepository extends JpaRepository<DemoGroupEntity,Long> {

}
