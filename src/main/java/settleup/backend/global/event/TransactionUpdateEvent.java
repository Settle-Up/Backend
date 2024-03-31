//package settleup.backend.global.event;
//
//import org.springframework.context.ApplicationEvent;
//import settleup.backend.domain.transaction.entity.TransactionalEntity;
//
//import java.util.concurrent.CompletableFuture;
//
//public class TransactionUpdateEvent extends ApplicationEvent {
//    private String transactionId;
//    private CompletableFuture<TransactionalEntity> completableFuture;
//
//    public TransactionUpdateEvent(Object source, String transactionId) {
//        super(source);
//        this.transactionId = transactionId;
//        this.completableFuture = new CompletableFuture<>();
//    }
//
//    public String getTransactionId() {
//        return transactionId;
//    }
//
//    public CompletableFuture<TransactionalEntity> getCompletableFuture() {
//        return completableFuture;
//    }
//}
