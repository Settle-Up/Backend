package settleup.backend.domain.receipt.receiptCommons;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import settleup.backend.domain.receipt.receiptCommons.ReceiptCreatedEvent;
import settleup.backend.domain.transaction.service.TransactionSagaService;

@Component
public class ReceiptEventListener {

    private final TransactionSagaService transactionSagaService;

    public ReceiptEventListener(TransactionSagaService transactionSagaService) {
        this.transactionSagaService = transactionSagaService;
    }

    @EventListener
    public void onReceiptCreatedEvent(ReceiptCreatedEvent event) {
        transactionSagaService.performOptimizationOperations(event.getTransactionDto());
    }
}