package kr.hhplus.be.server.application.coupon;

public record CouponIssueEvent(Long userId, Long couponId) {}