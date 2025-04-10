package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.api.order.OrderRequest.OrderItem;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class OrderFacade {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;



    // 주문
    public OrderDto createOrder(OrderCommand command) {
        Long totalAmount = productService.readOrder(command.orderItem());
        CouponDiscountResult discount = couponService.useCoupon(command.userCouponId());
        Long orderId = orderService.createOrder(command,totalAmount,discount);
        return new OrderDto(orderId);
    }


}
