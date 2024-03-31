//package settleup.backend.global.event;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//import settleup.backend.domain.transaction.entity.TransactionalEntity;
//import settleup.backend.domain.transaction.service.Impl.TransactionStrategySelector;
//import settleup.backend.global.exception.CustomException;
//
//
//@Component
//public class TransactionUpdateEventListener {
//
//    @Autowired
//    private TransactionStrategySelector strategySelector;
//
//    @EventListener
//    public void onTransactionUpdate(TransactionUpdateEvent event) {
//        try {
//            TransactionalEntity result = strategySelector.selectRepository(event.getTransactionId());
//            event.getCompletableFuture().complete(result); // 성공 결과 설정
//        } catch (CustomException e) {
//            event.getCompletableFuture().completeExceptionally(e); // 예외 설정
//        }
//    }
//}
