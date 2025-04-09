package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;


    // 주문 : 주문 상태, 토탈 주문 금액 insert
    @Transactional
    public Long createOrder(OrderCommand command, Long amount, CouponDiscountResult discount) {
        Long totalAmount = 0L;  // 총 할인가
        if(discount.discountType().name().equals("RATE")){
            totalAmount = amount - (amount*discount.discountValue()/100);
        }else if(discount.discountType().name().equals("AMOUNT")){
            totalAmount = amount- discount.discountValue();
        }

        OrderEntity order = new OrderEntity();
        order.setUserId(command.userId());
        order.setTotalAmount(totalAmount);
        order.setUserCouponId(command.userCouponId());

        OrderEntity saved = orderRepository.save(order);

        return saved.getId();
    }

    public OrderEntity readOrder(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(
            OrderErrorCode.ORDER_NOT_FOUND));
    }

    public void updateOrderStatus(Long orderId){
        orderRepository.updateOrderStatus(orderId);
    }

}
