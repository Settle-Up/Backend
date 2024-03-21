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
    public void performAsyncOperations(TransactionDto transactionDto) {
        TransactionDto transactionDto1 = requireService.createExpense(transactionDto);
        List<NetDto> netList = netService.calculateNet(transactionDto1);
        List<Long> optimizedP2PList=optimizedService.optimizationOfp2p(transactionDto1);
        groupOptimizedService.optimizationInGroup(optimizedP2PList,netList);
    }
    }

//
//        return requireService.createExpense(transactionDto)
//            .thenCompose(resultId -> optimizedService.optimizationOfp2p(resultId))
//            .exceptionally(ex -> {
//        // Sentry를 이용해 로그를 남깁니다.
//        Sentry.captureException(ex);
//        // exceptionally 블록 안에서 예외를 던지는 것은 권장되지 않습니다.
//        // 이 블록은 예외를 처리하기 위한 것이지, 전파하기 위한 것이 아닙니다.
//        // 실패를 나타내려면 다른 방법을 고려해야 합니다.
//        return null;
//    });
//}
//}