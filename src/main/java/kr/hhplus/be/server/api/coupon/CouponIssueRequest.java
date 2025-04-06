package kr.hhplus.be.server.api.coupon;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CouponIssueRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Positive(message = "userId는 양의 정수여야 합니다.")
    private Long userId;

    @NotNull(message = "couponId는 필수입니다.")
    @Positive(message = "couponId는 양의 정수여야 합니다.")
    private Long couponId;
}