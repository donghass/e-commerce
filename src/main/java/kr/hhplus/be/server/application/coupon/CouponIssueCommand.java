package kr.hhplus.be.server.application.coupon;

//controller  에서 application 로 받아오는 dto
public record CouponIssueCommand(Long userId, Long couponId) {
}
