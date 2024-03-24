package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;

public interface FinalOptimizedService {
    void lastMergeTransaction(TransactionP2PResultDto resultDto);
}
