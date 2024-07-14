package settleup.backend.domain.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.repository.AbstractGroupUserRepository;

@Service
@AllArgsConstructor
public class AbstractGroupUserDeletionService implements RelatedEntityDeletionService {

    private final AbstractGroupUserRepository abstractGroupUserRepository;

    @Override
    @Transactional
    public void deleteRelatedEntities(Long userId) {
        abstractGroupUserRepository.deleteByUserId(userId);
    }
}