package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.GroupEntity;

import java.util.Optional;


@Repository
public interface GroupRepository extends GroupBaseRepository<GroupEntity> {
}
//    Optional<GroupEntity> findByGroupUUID(String UUID);

