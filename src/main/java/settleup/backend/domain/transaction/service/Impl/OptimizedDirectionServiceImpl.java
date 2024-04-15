package settleup.backend.domain.transaction.service.Impl;

import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.domain.user.entity.dto.UserGroupDto;

import java.util.List;

@AllArgsConstructor
@Service
public class OptimizedDirectionServiceImpl implements OptimizedDirectionService {
    private final GroupOptimizedService groupOptimizedService;
    private final OptimizedService optimizedService;
    private final NetService netService;
    private final UltimateOptimizedService ultimateOptimizedService;

    @Override
    @Transactional
    public void performOptimizationOperations(UserGroupDto userGroupDto) {
        try {
            List<NetDto> netList = netService.calculateNet(userGroupDto);
            TransactionP2PResultDto resultDto = optimizedService.optimizationOfp2p(userGroupDto);

            if (groupOptimizedService.optimizationInGroup(resultDto, netList)) {
                ultimateOptimizedService.ultimateOptimizedTransaction(resultDto);
            }

        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
