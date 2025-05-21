package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import kr.hhplus.be.server.domain.point.PointEvent;
import kr.hhplus.be.server.domain.point.PointEventInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSpringEventPublisher implements OrderEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishCompleted(OrderEntity order) {
        applicationEventPublisher.publishEvent(new OrderEvent.OrderCompletedEvent(order));
    }

    @Override
    public void publishOrderUpdateFailedEvent(PointEventInfo event) {
        applicationEventPublisher.publishEvent(new PointEvent.PointUsedCompleted(event));
    }
}
