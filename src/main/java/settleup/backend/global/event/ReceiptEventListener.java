package settleup.backend.global.event;

import io.sentry.Sentry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import settleup.backend.global.event.ReceiptCreatedEvent;
import settleup.backend.domain.transaction.service.TransactionSagaService;

import java.util.concurrent.CompletableFuture;

//@Component
//public class ReceiptEventListener {
//
//    private final TransactionSagaService transactionSagaService;
//
//    public ReceiptEventListener(TransactionSagaService transactionSagaService) {
//        this.transactionSagaService = transactionSagaService;
//    }
//
//    @EventListener
//    public void onReceiptCreatedEvent(ReceiptCreatedEvent event) {
//        transactionSagaService.performOptimizationOperations(event.getTransactionDto());
//    }
//}
@Component
public class ReceiptEventListener {

    private final TransactionSagaService transactionSagaService;

    public ReceiptEventListener(TransactionSagaService transactionSagaService) {
        this.transactionSagaService = transactionSagaService;
    }

    @EventListener
    public void onReceiptCreatedEvent(ReceiptCreatedEvent event) {
        CompletableFuture.runAsync(() ->
                        transactionSagaService.performOptimizationOperations(event.getTransactionDto()))
                .exceptionally(ex -> {
                    Sentry.captureException(ex);
                    return null;
                });
    }
}
