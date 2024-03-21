package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.NetDto;

import java.util.List;

public interface GroupOptimizedService {
    void optimizationInGroup(List<Long> p2pList, List<NetDto> net);
}
