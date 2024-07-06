package settleup.backend.domain.group.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;


import java.util.List;

@Repository
public class CustomGroupUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<GroupUserTypeEntity> findByUserIdWithLatestReceiptOrCreatedAt(Long userId, Pageable pageable, Boolean isRegularUser) {
        String groupUserTable = isRegularUser ? "settle_group_user" : "settle_demo_group_user";
        String groupTable = isRegularUser ? "settle_group" : "settle_demo_group";

        String queryString = "SELECT gue.* FROM " + groupUserTable + " gue " +
                "JOIN " + groupTable + " g ON gue.group_id = g.id " +
                "LEFT JOIN (SELECT group_id, MAX(created_at) AS last_active FROM receipt GROUP BY group_id) r ON g.id = r.group_id " +
                "WHERE gue.user_id = :userId " +
                "ORDER BY CASE WHEN r.last_active IS NULL THEN 1 ELSE 0 END, COALESCE(r.last_active, g.created_at) DESC";

        Query query = entityManager.createNativeQuery(queryString, isRegularUser ? GroupUserEntity.class : DemoGroupUserEntity.class)
                .setParameter("userId", userId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<GroupUserTypeEntity> result = query.getResultList();
        return new PageImpl<>(result, pageable, result.size());
    }
}
