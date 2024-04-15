package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;

import java.util.List;

public interface GroupOptimizedService extends TransactionProcessingService {
    boolean optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net);
}
