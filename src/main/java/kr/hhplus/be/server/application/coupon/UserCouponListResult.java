package kr.hhplus.be.server.application.coupon;

import java.util.List;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;

public record UserCouponListResult(Long userId, List<UserCouponWithCouponDto> userCouponList) {

}