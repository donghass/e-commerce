package kr.hhplus.be.server.domain.coupon;

// 주문 쿠폰 적용
public record CouponApplyResult(UserCouponEntity userCoupon, CouponEntity coupon) {}