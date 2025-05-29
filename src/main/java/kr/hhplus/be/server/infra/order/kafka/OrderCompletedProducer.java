package kr.hhplus.be.server.infra.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEvent.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import kr.hhplus.be.server.domain.order.kafka.KafkaOrderProducer;
import kr.hhplus.be.server.domain.point.PointEventInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedProducer implements KafkaOrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishCompleted(OrderEntity order) {
        OrderCompletedEvent event = new OrderCompletedEvent(order); // DTO 변환
        log.info("Kafka 메시지 발행 시작");

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order.completed", json).get(); // Kafka에 전송
            log.info("Kafka 메시지 발행 성공: orderId={}", event.order());
        } catch (Exception e) {
            log.error("Kafka 메시지 발행 실패", e);
        }
    }

    @Override
    public void publishOrderUpdateFailedEvent(PointEventInfo event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order.failed", json);
            log.info("Kafka 실패 이벤트 발행: orderId={}", event.getOrder().getId());
        } catch (Exception e) {
            log.error("Kafka 실패 이벤트 발행 실패", e);
        }
    }
}