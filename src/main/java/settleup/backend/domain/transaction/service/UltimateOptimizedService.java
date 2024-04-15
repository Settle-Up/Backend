package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;

public interface UltimateOptimizedService extends TransactionProcessingService {
    void ultimateOptimizedTransaction(TransactionP2PResultDto resultDto);
}
