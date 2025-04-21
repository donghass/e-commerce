package kr.hhplus.be.server.domain.coupon;

import java.util.Optional;

public enum DiscountPolicy {
    RATE,      // 정률
    AMOUNT      // 정액
    ;


    public static Long discount(Long amount, CouponEntity coupon) {
        if (coupon == null) {
            return 0L; // 할인 없음
        }
        Long totalAmount = 0L;  // 총 할인가

        if(coupon.getDiscountType().name().equals(RATE)){
            totalAmount = amount - (amount*coupon.getDiscountValue()/100);
        }else if(coupon.getDiscountType().name().equals(AMOUNT)){
            totalAmount = amount- coupon.getDiscountValue();
        }
        return totalAmount;
    }
}
