package kr.hhplus.be.server.infra.coupon.kafka;

import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class CouponIssueConsumer {

    private final CouponService couponService;

    @KafkaListener(
        topics = "coupon.issue",
        groupId = "coupon-consumer-group",
        containerFactory = "couponKafkaListenerContainerFactory"  // 아래 설정 참고
    )
    public void consume(CouponIssueCommand command) {
        try {
            log.info("쿠폰 컨슈머 성공");
            couponService.couponIssued(command);
        } catch (Exception e) {
            // 예외를 삼키면 안 되고 반드시 던져야 DLQ 전송됨
            throw new RuntimeException("쿠폰 발급 처리 실패", e);
        }
    }
}