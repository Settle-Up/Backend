package settleup.backend.domain.user.repository;

public interface RelatedEntityDeletionService {
    void deleteRelatedEntities(Long userId);
}