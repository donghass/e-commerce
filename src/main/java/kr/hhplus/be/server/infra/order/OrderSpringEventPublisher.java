package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSpringEventPublisher implements OrderEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishCompleted(OrderEntity order) {
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(order));
    }
}
