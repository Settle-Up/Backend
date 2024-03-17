package settleup.backend.domain.transaction.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.transaction.entity.dto.OptimizationTargetDto;
import settleup.backend.global.exception.CustomException;


public interface OptimizedService {

    void optimizationOfPersonal(OptimizationTargetDto targetDto) throws CustomException;
    void optimizationOfNet(OptimizationTargetDto targetDto) throws CustomException;
    void optimizationOfGroup(OptimizationTargetDto targetDto) throws CustomException;
}
