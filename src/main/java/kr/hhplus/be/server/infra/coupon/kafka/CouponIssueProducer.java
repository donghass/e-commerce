package kr.hhplus.be.server.infra.coupon.kafka;

import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.kafka.CouponProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class CouponIssueProducer implements CouponProducer {

    private final KafkaTemplate<String, CouponIssueCommand> kafkaTemplate;
    private static final String TOPIC = "coupon.issue";

    @Override
    public void publishCompleted(CouponIssueCommand command) {
        try {
            String key = String.valueOf(command.couponId()); // couponId 기준 파티션
            kafkaTemplate.send(TOPIC, key, command).get(); // Future.get()으로 대기
            log.info("카프카 쿠폰 발행 메시지 발행 성공");
        } catch (Exception e) {
            log.error("카프카 발행 중 예외 발생", e);
        }
    }
}
