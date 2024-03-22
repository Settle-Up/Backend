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

    @Override
    public void performOptimizationOperations(TransactionDto transactionDto) {
        try {
            TransactionDto transactionDto1 = requireService.createExpense(transactionDto);

            CompletableFuture<List<NetDto>> netListFuture = CompletableFuture.supplyAsync(() -> netService.calculateNet(transactionDto1));
            CompletableFuture<List<Long>> optimizedP2PListFuture = CompletableFuture.supplyAsync(() -> optimizedService.optimizationOfp2p(transactionDto1));

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(netListFuture, optimizedP2PListFuture);

            allFutures.thenAccept(v -> {
                List<NetDto> netList = netListFuture.join();
                List<Long> optimizedP2PList = optimizedP2PListFuture.join();

                groupOptimizedService.optimizationInGroup(optimizedP2PList, netList);
            }).exceptionally(e -> {
                Sentry.captureException(e);
                return null;
            }).join();
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
