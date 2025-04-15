package kr.hhplus.be.server.application.order;

public class DiscountPolicy {
    public enum DiscountType {
        RATE,   // 비율 할인
        AMOUNT  // 금액 할인
    }

    public long calculateDiscountedAmount(DiscountType discountType, Long discountValue, long amount) {
        long totalAmount = 0L;

        if (discountType == DiscountType.RATE) {
            totalAmount = amount - (long) (amount * discountValue / 100);
        } else if (discountType == DiscountType.AMOUNT) {
            totalAmount = amount - (long) discountValue;
        }

        return totalAmount;
    }
}