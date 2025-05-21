package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.order.DataPlatformClient;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.point.PointEvent;
import kr.hhplus.be.server.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @Async("taskExecutor")  // taskExecutor 빈 등록 필요
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderUpdateFailedEvent(PointEvent.PointUsedCompleted event) {
        pointService.rollbackPoint(event);
    }
}