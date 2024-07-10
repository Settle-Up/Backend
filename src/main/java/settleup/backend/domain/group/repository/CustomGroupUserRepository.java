package settleup.backend.domain.group.repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import jakarta.persistence.Query;

import java.util.List;

@Repository
public class CustomGroupUserRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomGroupUserRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    public Page<AbstractGroupUserEntity> findByUserIdWithLatestReceiptOrCreatedAt(Long userId, Pageable pageable, Boolean isRegularUser) {
        String groupUserTable = isRegularUser ? "settle_group_user" : "settle_demo_group_user";
        String groupTable = isRegularUser ? "settle_group" : "settle_demo_group";
        String abstractGroupTable = "abstract_group"; // 테이블 이름에 맞게 수정 필요

        String queryString = "SELECT agu.* FROM abstract_group_user agu " +
                "JOIN " + groupUserTable + " gue ON agu.id = gue.id " +
                "JOIN " + abstractGroupTable + " ag ON agu.group_id = ag.id " +
                "LEFT JOIN (SELECT group_id, MAX(created_at) AS last_active FROM receipt GROUP BY group_id) r ON ag.id = r.group_id " +
                "WHERE agu.user_id = :userId " +
                "ORDER BY CASE WHEN r.last_active IS NULL THEN 1 ELSE 0 END, COALESCE(r.last_active, ag.created_at) DESC";

        logger.debug("Executing query: {}", queryString);
        logger.debug("Parameters - userId: {}, pageable: {}, isRegularUser: {}", userId, pageable, isRegularUser);

        Query query = entityManager.createNativeQuery(queryString, isRegularUser ? GroupUserEntity.class : DemoGroupUserEntity.class)
                .setParameter("userId", userId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<AbstractGroupUserEntity> result = query.getResultList();
        logger.debug("Query result: {}", result);

        return new PageImpl<>(result, pageable, result.size());
    }
}