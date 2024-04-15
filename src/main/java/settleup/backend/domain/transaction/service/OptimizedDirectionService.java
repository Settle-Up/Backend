package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.global.exception.CustomException;

public interface OptimizedDirectionService {
    void performOptimizationOperations(UserGroupDto userGroupDto) throws CustomException;
}
