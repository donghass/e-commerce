목적
기존 Spring 내장 이벤트 기반으로 처리되던 선착순 쿠폰 발급 로직을 Kafka 기반의 비동기 메시징 시스템으로 변경하여 대용량 트래픽에도 견딜 수 있는 확장성, 장애 분리, 메시지 유실 방지, 재처리 용이성을 확보한다.


설계 시퀀스다이어그램



Kafka 구성

| 항목              | 구성 내용 |
|-------------------|-----------|
| **Topic**         | `coupon.issue` |
| **Partition 수**  | `P ≥ 예상 동시 사용자 수 / 컨슈머 수` |
| **Replication**   | `3` (기본 고가용성 설정) |
| **Key**           | `couponId` (쿠폰별 파티셔닝 보장) |
| **Producer 방식** | `kafkaTemplate.send().get()` 사용으로 동기 처리 및 예외 감지 가능 |
| **Consumer 설정** | `ConcurrentKafkaListenerContainerFactory`를 통한 멀티스레드 처리 |
| **DLQ (Dead Letter Queue)** | `coupon.issue.DLQ` (처리 실패 메시지 재처리용 별도 토픽) |
| **Offset 관리**   | `KafkaConfig` 설정을 통한 `manual-ack` (offset 수동 커밋 방식) |

### Kafka Producer
```java
public void publishCompleted(CouponIssueCommand command) {
        try {
            String key = String.valueOf(command.couponId()); // couponId 기준 파티션
            kafkaTemplate.send(TOPIC, key, command).get(); // Future.get()으로 대기
            log.info("카프카 쿠폰 발행 메시지 발행 성공");
        } catch (Exception e) {
            log.error("카프카 발행 중 예외 발생", e);
        }
    }
```
### Kafka Consumer
```java
@KafkaListener(
        topics = "coupon.issue",
        groupId = "coupon-consumer-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void consume(CouponIssueCommand command, Acknowledgment ack) {
        try {
            couponService.couponIssued(command);
            // 수동 커밋
            ack.acknowledge();
        } catch (Exception e) {
            // 예외를 삼키면 안 되고 반드시 던져야 DLQ 전송됨
            throw new RuntimeException("쿠폰 발급 처리 실패", e);
        }
    }
```

## 결론
Kafka를 기반으로 비동기 발급 구조를 갖춤으로써 다음을 기대할 수 있습니다:

트래픽 피크 시에도 안정적인 처리

발급 실패 메시지 재처리 가능

처리 병렬화로 속도 향상

서비스 간 느슨한 결합과 장애 전파 최소화

