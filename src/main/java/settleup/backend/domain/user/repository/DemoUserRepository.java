package settleup.backend.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.user.entity.DemoUserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemoUserRepository extends UserBaseRepository<DemoUserEntity> {
}
