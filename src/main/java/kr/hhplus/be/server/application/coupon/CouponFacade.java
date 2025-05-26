package kr.hhplus.be.server.application.coupon;

import jakarta.validation.Valid;
import java.util.List;
import kr.hhplus.be.server.domain.coupon.CouponIssueEvent;
import kr.hhplus.be.server.domain.coupon.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;
import kr.hhplus.be.server.domain.coupon.kafka.CouponProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class CouponFacade {
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;
    private final CouponRedisRepository couponRedisRepository;
    private final CouponProducer couponProducer;

    public void createCoupon(@Valid CouponIssueCommand command) {
        couponService.createCoupon(command);
    }

    public UserCouponListResult getUserCoupons(Long userId) {
        List<UserCouponWithCouponDto> userCouponList = couponService.userCouponList(userId);
        return new UserCouponListResult(userId, userCouponList);
    }

    public Long issueCouponAsync(CouponIssueCommand command) {
        Long userId = command.userId();
        Long couponId = command.couponId();
        String stockKey = "coupon:stock:" + couponId;
        String issuedKey = "coupon:issued:" + couponId;

        Long result = couponRedisRepository.tryIssue(issuedKey, stockKey, userId.toString());

        if(result == 1) {
//            eventPublisher.publishEvent(new CouponIssueEvent(command.userId(), command.couponId()));
            // 기존 로직 제거, 카프카 메시지 발행
            couponProducer.publishCompleted(command);
        }
        return result;
    }
}
