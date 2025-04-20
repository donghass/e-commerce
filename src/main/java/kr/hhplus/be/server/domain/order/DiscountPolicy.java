package kr.hhplus.be.server.domain.order;

import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;

public enum DiscountPolicy {
    RATE,      // 정률
    AMOUNT      // 정액
    ;


    public static Long discount(Long amount, Optional<CouponDiscountResult> discount) {
        if (discount.isEmpty()) {
            return 0L; // 할인 없음
        }
        Long totalAmount = 0L;  // 총 할인가
        CouponDiscountResult coupon = discount.get(); // 꺼냄
        if(coupon.discountType().name().equals(RATE)){
            totalAmount = amount - (amount*coupon.discountValue()/100);
        }else if(coupon.discountType().name().equals(AMOUNT)){
            totalAmount = amount- coupon.discountValue();
        }
        return totalAmount;
    }
}
