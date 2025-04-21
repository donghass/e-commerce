package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;

public record UserCouponWithCouponDto(
    Long userCouponId,
    Boolean isUsed,
    LocalDateTime expiredDate,
    Long couponId,
    String name,
    DiscountType discountType,
    Long discountValue
) {}