package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.dto.OptimizationTargetDto;
import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.global.exception.CustomException;
@Service
@AllArgsConstructor
@Transactional
public class OptimizedServiceImpl implements OptimizedService {
    @Override
    public void optimizationOfPersonal(OptimizationTargetDto targetDto) throws CustomException {

    }

    @Override
    public void optimizationOfNet(OptimizationTargetDto targetDto) throws CustomException {

    }

    @Override
    public void optimizationOfGroup(OptimizationTargetDto targetDto) throws CustomException {

    }
}
