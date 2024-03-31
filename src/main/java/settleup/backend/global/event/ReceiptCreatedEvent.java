package settleup.backend.global.event;

import org.springframework.context.ApplicationEvent;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;

public class ReceiptCreatedEvent extends ApplicationEvent {
    private TransactionDto transactionDto;

    public ReceiptCreatedEvent(Object source, TransactionDto transactionDto) {
        super(source);
        this.transactionDto = transactionDto;
    }

    public TransactionDto getTransactionDto() {
        return transactionDto;
    }
}
