package settleup.backend.domain.transaction.service.Impl;

import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.global.exception.CustomException;

import java.util.List;

@AllArgsConstructor
@Service
public class OptimizedDirectionServiceImpl implements OptimizedDirectionService {
    private final GroupOptimizedService groupOptimizedService;
    private final OptimizedService optimizedService;
    private final NetService netService;
    private final UltimateOptimizedService ultimateOptimizedService;

    private static final Logger logger = LoggerFactory.getLogger(OptimizedDirectionServiceImpl.class);

    @Override
    @Transactional
    public void performOptimizationOperations(UserGroupDto userGroupDto) {
        try {
            List<NetDto> netList = netService.calculateNet(userGroupDto);
            TransactionP2PResultDto resultDto = optimizedService.optimizationOfp2p(userGroupDto);

            if (groupOptimizedService.optimizationInGroup(resultDto, netList)) {
                ultimateOptimizedService.ultimateOptimizedTransaction(resultDto);
            }
            logger.info("performOptimizationOperations OK");

        } catch (CustomException e) {
            logger.error("CustomException in performOptimizationOperations: ", e);
            Sentry.captureException(e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in performOptimizationOperations: ", e);
            Sentry.captureException(e);
            throw e;
        }
    }
}