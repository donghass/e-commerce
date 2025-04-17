package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;

public record CouponDiscountResult(Long discountValue, DiscountType discountType) {}