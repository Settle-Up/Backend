package settleup.backend.domain.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;

@Repository
public interface AbstractGroupUserRepository extends JpaRepository<AbstractGroupUserEntity, Long> {
    void deleteByUserId(Long userId);
}