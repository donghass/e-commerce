package kr.hhplus.be.server.api.coupon;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import lombok.Getter;

public record CouponIssueRequest(
    @NotNull(message = "userId는 필수입니다.")
    @Positive(message = "userId는 양의 정수여야 합니다.")
    Long userId,

    @NotNull(message = "couponId는 필수입니다.")
    @Positive(message = "couponId는 양의 정수여야 합니다.")
    Long couponId
) {
    public CouponIssueCommand toCommand() {
        return new CouponIssueCommand(userId, couponId);
    }
}