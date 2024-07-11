package settleup.backend.domain.group.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoGroupUserRepository extends GroupUserBaseRepository<DemoGroupUserEntity> {
}

