package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.DataPlatformClient;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.kafka.KafkaOrderProducer;
import kr.hhplus.be.server.domain.point.PointEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final DataPlatformClient dataPlatformClient;
    private final OrderService orderService;
    private final KafkaOrderProducer kafkaOrderProducer;

//    @Async("taskExecutor")  // taskExecutor 빈 등록 필요
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderSendCompleted(OrderEvent.OrderCompletedEvent event) {
//        dataPlatformClient.sendToDataPlatform(event.order());
        // 카프카 메시지 발행
        kafkaOrderProducer.publishCompleted(event.order());
    }

    @Async("taskExecutor")  // taskExecutor 빈 등록 필요
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointUsedCompleted(PointEvent.PointUsedCompleted event) {
        orderService.updateOrderStatus(event.pointEventInfo());
    }
}