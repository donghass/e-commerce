package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;

public enum DiscountPolicy {
    RATE,      // 정률
    AMOUNT      // 정액
    ;


    public static Long discount(Long amount, CouponDiscountResult discount) {
        Long totalAmount = 0L;  // 총 할인가
        if(discount.discountType().name().equals(RATE)){
            totalAmount = amount - (amount*discount.discountValue()/100);
        }else if(discount.discountType().name().equals(AMOUNT)){
            totalAmount = amount- discount.discountValue();
        }
        return totalAmount;
    }
}
