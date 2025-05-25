package kr.hhplus.be.server.domain.order.kafka;

import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.point.PointEventInfo;

public interface KafkaOrderProducer {
    void publishCompleted(OrderEntity order);

    void publishOrderUpdateFailedEvent(PointEventInfo order);
}
