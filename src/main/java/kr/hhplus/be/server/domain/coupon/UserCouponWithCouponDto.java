package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponWithCouponDto {
    private Long userCouponId;
    private Boolean isUsed;
    private LocalDateTime expiredDate;
    private Long couponId;
    private String name;
    private DiscountType discountType;
    private Long discountValue;
}
