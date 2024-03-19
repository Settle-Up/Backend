package settleup.backend.domain.transaction.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.global.exception.CustomException;

import java.util.concurrent.CompletableFuture;


public interface OptimizedService {

    CompletableFuture<Void> optimizationOfp2p(TransactionDto targetDto) throws CustomException;
//    void optimizationOfNet(OptimizationTargetDto targetDto) throws CustomException;
//    void optimizationOfGroup(OptimizationTargetDto targetDto) throws CustomException;
}
