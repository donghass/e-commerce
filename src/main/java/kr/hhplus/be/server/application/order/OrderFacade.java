package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class OrderFacade {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;



    // 주문
    @Transactional
    public OrderResult createOrder(OrderCommand command) {
        // 주문 상품으로 주문총액 조회 및 재고 차감
        Long totalAmount = productService.readOrderProduct(command.orderItem());
        // 쿠폰 할인 가격 조회
        CouponDiscountResult discount = couponService.useCoupon(command.userCouponId());
        // 주문 생성
        Long orderId = orderService.createOrder(command,totalAmount,discount);
        return new OrderResult(orderId);
    }


}
