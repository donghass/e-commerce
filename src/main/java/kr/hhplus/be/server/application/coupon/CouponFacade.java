package kr.hhplus.be.server.application.coupon;

import jakarta.validation.Valid;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class CouponFacade {
    private CouponService couponService;

    public void createCoupon(@Valid CouponIssueCommand command) {
        couponService.createCoupon(command);
    }
}
