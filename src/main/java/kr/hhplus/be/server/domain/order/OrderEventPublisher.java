package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.point.PointEventInfo;

public interface OrderEventPublisher {
    void publishCompleted(OrderEntity order);

    void publishOrderUpdateFailedEvent(PointEventInfo order);
}
