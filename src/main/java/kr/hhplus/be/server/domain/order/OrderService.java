package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;


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
// 주문 조회
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
        LocalDateTime expiredOrderTime = LocalDateTime.now().minusMinutes(5); // 현시간 - 5분
        List<OrderEntity> expiredOrders = orderRepository.findNotPaidOrdersOlderThan(expiredOrderTime);

        // 주문에서는 쿠폰 금액 차감만 하고 쿠폰 사용저리는 결제때 구현으로 변경
        // 주문상품 별 갯수 조회하여 상품 재고 원복
        for (OrderEntity order : expiredOrders) {
            orderRepository.updateStatus(order.getId(), PaymentStatus.EXPIRED);

            Optional<OrderProductEntity> orderProduct = orderRepository.findByOrderId(order.getId());
            Optional<ProductEntity> product = productRepository.findById(orderProduct.get().getProductId());

            Long nowStock = orderProduct.get().getQuantity() + product.get().getStock();
            productRepository.updateStock(product.get().getId(), nowStock);
        }
    }
}
