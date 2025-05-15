package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.CouponEntity;

public record CouponIssueEvent(Long userId, Long couponId) {}