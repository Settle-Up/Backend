package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionInheritanceServiceImpl implements TransactionInheritanceService {
    private final OptimizedTransactionDetailsRepository optimizedTransactionDetailsRepo;
    private final RequireTransactionRepository requireTransactionRepo;
    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedTransactionDetailRepo;

    @Transactional
    public void clearInheritanceStatusForOptimizedToRequired(Long optimizedTransactionId) {
        List<OptimizedTransactionDetailsEntity> inheritedTargetList = optimizedTransactionDetailsRepo.findByOptimizedTransactionId(optimizedTransactionId);
        for (OptimizedTransactionDetailsEntity inheritedTarget : inheritedTargetList) {
            requireTransactionRepo.updateInheritanceStatusToClearById(inheritedTarget.getRequiresTransaction().getId());
            LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
            requireTransactionRepo.updateClearStatusTimestampById(inheritedTarget.getRequiresTransaction().getId(),newClearStatusTimestamp);
        }
    }

    @Transactional
    public void clearInheritanceStatusForGroupToOptimized(Long optimizedTransactionId) {
        optimizedTransactionRepo.updateInheritanceStatusToClearById(optimizedTransactionId);
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        optimizedTransactionRepo.updateClearStatusTimestampById(optimizedTransactionId,newClearStatusTimestamp);
        clearInheritanceStatusForOptimizedToRequired(optimizedTransactionId);
    }


    @Transactional
    public void clearInheritanceStatusForFinalToGroup(Optional<GroupOptimizedTransactionEntity> groupOptimizedTransaction) {
        groupOptimizedTransactionRepo.updateInheritanceStatusToClearById(groupOptimizedTransaction.get().getId());
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        groupOptimizedTransactionRepo.updateClearStatusTimestampById(groupOptimizedTransaction.get().getId(),newClearStatusTimestamp);

        // group 은 바꿈 -> 다음 그 group에 연결된 optimized_id 들을 보내야함
        List<GroupOptimizedTransactionDetailsEntity> goesToGroupToOptimizedList =
                groupOptimizedTransactionDetailRepo.findByGroupOptimizedTransactionId(groupOptimizedTransaction.get().getId());
        for (GroupOptimizedTransactionDetailsEntity goesToGroupToOptimized : goesToGroupToOptimizedList) {
            clearInheritanceStatusForGroupToOptimized(goesToGroupToOptimized.getOptimizedTransaction().getId());
        }

    }

    @Override
    @Transactional
    public void clearInheritanceStatusForFinalToOptimized(Optional<OptimizedTransactionEntity> optimizedTransaction) {
        optimizedTransactionRepo.updateInheritanceStatusToClearById(optimizedTransaction.get().getId());
        LocalDateTime newClearStatusTimestamp = LocalDateTime.now();
        optimizedTransactionRepo.updateClearStatusTimestampById(optimizedTransaction.get().getId(),newClearStatusTimestamp);
        clearInheritanceStatusForOptimizedToRequired(optimizedTransaction.get().getId());
    }

}