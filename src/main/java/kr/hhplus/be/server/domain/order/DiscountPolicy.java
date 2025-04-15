package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;

public enum DiscountPolicy {
    NOT_PAID,      // 미결제
    PAID,      // 결제
    EXPIRED     // 주문 시간 만료
    ;


    public static Long discount(Long amount, CouponDiscountResult discount) {
        Long totalAmount = DiscountPolicy.discount(amount, discount);  // 총 할인가
        if(discount.discountType().name().equals("RATE")){
            totalAmount = amount - (amount*discount.discountValue()/100);
        }else if(discount.discountType().name().equals("AMOUNT")){
            totalAmount = amount- discount.discountValue();
        }
        return totalAmount;
    }
}
