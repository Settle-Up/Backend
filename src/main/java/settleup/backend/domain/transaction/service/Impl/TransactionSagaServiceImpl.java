package settleup.backend.domain.transaction.service.Impl;

import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
import settleup.backend.domain.transaction.service.*;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
@Transactional
public class TransactionSagaServiceImpl implements TransactionSagaService {
    private final RequireTransactionService requireService;
    private final GroupOptimizedService groupOptimizedService;
    private final OptimizedService optimizedService;
    private final NetService netService;
    private final FinalOptimizedService mergeTransaction;

    @Override
    public void performOptimizationOperations(TransactionDto transactionDto) {
        try {
            // createExpense 호출하여 영수증 처리
            TransactionDto transactionDto1 = requireService.createExpense(transactionDto);

            // Net 계산을 동기적으로 처리
            List<NetDto> netList = netService.calculateNet(transactionDto1);

            // P2P 최적화를 동기적으로 처리
            TransactionP2PResultDto resultDto = optimizedService.optimizationOfp2p(transactionDto1);

            // 그룹 내 최적화를 동기적으로 처리
            groupOptimizedService.optimizationInGroup(resultDto, netList);
            mergeTransaction.lastMergeTransaction(resultDto);

        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
