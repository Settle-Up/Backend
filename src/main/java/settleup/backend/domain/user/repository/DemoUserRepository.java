package settleup.backend.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.DemoUserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemoUserRepository extends UserBaseRepository<DemoUserEntity> {
        @Query("SELECT d.id FROM DemoUserEntity d WHERE d.createdAt < :expirationTime AND d.isDummy = false")
        List<Long> findExpiredNonDummyUserIds(@Param("expirationTime") LocalDateTime expirationTime);
        @Modifying
        @Transactional
        @Query("DELETE FROM DemoUserEntity d WHERE d.createdAt < :expirationTime AND d.ip NOT IN :excludedIps")
        int deleteByCreatedAtBefore(@Param("expirationTime") LocalDateTime expirationTime, @Param("excludedIps") List<String> excludedIps);

        @Query("SELECT d FROM DemoUserEntity d WHERE d.ip = :ip ORDER BY d.createdAt DESC")
        List<DemoUserEntity> findTopByIpOrderByCreatedAtDesc(@Param("ip") String ip, Pageable pageable);
}


