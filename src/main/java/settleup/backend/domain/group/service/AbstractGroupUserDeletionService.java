package settleup.backend.domain.group.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.repository.AbstractGroupUserRepository;
import settleup.backend.domain.user.repository.RelatedEntityDeletionService;

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