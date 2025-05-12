package kr.hhplus.be.server.application.coupon;

import jakarta.validation.Valid;
import java.util.List;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class CouponFacade {
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    public void createCoupon(@Valid CouponIssueCommand command) {
        couponService.createCoupon(command);
    }

    public UserCouponListResult getUserCoupons(Long userId) {
        List<UserCouponWithCouponDto> userCouponList = couponService.userCouponList(userId);
        return new UserCouponListResult(userId, userCouponList);
    }

    public void issueCouponAsync(CouponIssueCommand command) {
        eventPublisher.publishEvent(new CouponIssueEvent(command.userId(), command.couponId()));
    }
}
