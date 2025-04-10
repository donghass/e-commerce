package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
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

    // 5분 주기로 주문 생성 5분 지난 주문건 취소 스케줄러
    @Transactional
    public void expireOldUnpaidOrders() {
        LocalDateTime expiredOrder = LocalDateTime.now().minusMinutes(5);
        List<OrderEntity> expiredOrders = orderRepository.findUnpaidOrdersOlderThan(expiredOrder);

        for (OrderEntity order : expiredOrders) {
            order.updateStatus(PaymentStatus.EXPIRED);
        }

        // 변경 사항 자동 커밋 (JPA flush)
    }
}
