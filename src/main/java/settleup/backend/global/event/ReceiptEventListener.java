package settleup.backend.global.event;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import settleup.backend.domain.transaction.service.RequireTransactionService;

import java.util.concurrent.CompletableFuture;

@Component
public class ReceiptEventListener {

    private final RequireTransactionService requireTransactionService;


    private static final Logger log = LoggerFactory.getLogger(ReceiptEventListener.class);

    public ReceiptEventListener(RequireTransactionService requireTransactionService) {
        this.requireTransactionService = requireTransactionService;
    }

    @EventListener
    public void onReceiptCreatedEvent(ReceiptCreatedEvent event) {
        CompletableFuture.runAsync(() -> requireTransactionService.createExpense(event.getTransactionDto()))
                .exceptionally(ex -> {
                    log.error("트랜잭션 최적화 처리 중 오류 발생", ex);
                    Sentry.captureException(ex);
                    return null;
                });

    }
}
