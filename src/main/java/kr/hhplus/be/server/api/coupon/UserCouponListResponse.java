package kr.hhplus.be.server.api.coupon;

import java.util.List;
import kr.hhplus.be.server.application.coupon.UserCouponListResult;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;

public record UserCouponListResponse(Long userId, List<UserCouponWithCouponDto> coupons) {
    public static UserCouponListResponse from(UserCouponListResult result) {
        return new UserCouponListResponse(result.userId(), result.userCouponList());
    }
}