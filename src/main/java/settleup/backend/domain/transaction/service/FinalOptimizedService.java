package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;

public interface FinalOptimizedService extends TransactionProcessingService {
    void lastMergeTransaction(TransactionP2PResultDto resultDto);
}
