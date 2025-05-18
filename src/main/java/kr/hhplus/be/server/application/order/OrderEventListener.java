package kr.hhplus.be.server.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final DataPlatformClient dataPlatformClient;

    @Async("taskExecutor")  // taskExecutor 빈 등록 필요
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        dataPlatformClient.sendToDataPlatform(event.order());
    }
}