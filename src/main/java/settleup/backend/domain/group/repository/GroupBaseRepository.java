package settleup.backend.domain.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import settleup.backend.domain.group.entity.GroupTypeEntity;

import java.util.Optional;

@NoRepositoryBean
public interface GroupBaseRepository<T extends GroupTypeEntity> extends JpaRepository<T, Long> {
    Optional<T> findByGroupUUID(String groupUUID);

    @Query("SELECT g FROM #{#entityName} g WHERE g.groupName LIKE %:name%")
    Page<T> findByGroupNameContaining(@Param("name") String name, Pageable pageable);
}