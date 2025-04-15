package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProductRepository productRepository;


    // 주문 : 주문 상태, 토탈 주문 금액 insert
    public Long createOrder(OrderCommand command, Long amount, CouponDiscountResult discount) {
        Long totalAmount =0L;  // 총 할인가
        if(discount.discountType().name().equals("RATE")){
            totalAmount = amount - (amount*discount.discountValue()/100);
        }else if(discount.discountType().name().equals("AMOUNT")){
            totalAmount = amount- discount.discountValue();
        }

        // 도메인 객체 생성
        OrderEntity order = OrderEntity.create(command.userId(), command.userCouponId(), totalAmount);
        // 주문 생성
        OrderEntity saved = orderRepository.save(order);

        // 재고 차감 및 주문상품 생성
        for(int i = 0; i < command.orderItem().size(); i++){
            ProductEntity product = productRepository.findById(command.orderItem().get(i).productId())
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

            product.updateStock(command.orderItem().get(i).quantity());

            Long productAmount = 0l;
            Long orderProductAmount = productAmount * command.orderItem().get(i).quantity();;

            // 도메인 객체 생성
            OrderProductEntity orderProduct = OrderProductEntity.create(command.orderItem().get(i).productId(),
                saved.getId(), orderProductAmount, command.orderItem().get(i).quantity());
            orderRepository.orderItemSave(orderProduct);
        }

        return saved.getId();
    }
// 주문 조회
    public OrderEntity readOrder(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(
            OrderErrorCode.ORDER_NOT_FOUND));
    }

    public void updateOrderStatus(Long orderId){
        orderRepository.updateOrderStatus(orderId, PaymentStatus.PAID);
    }

    // 5분 주기로 주문 생성 5분 지난 주문건 취소 스케줄러
    @Transactional
    public void expireOldUnpaidOrders() {
        List<OrderEntity> expiredOrders = orderRepository.findNotPaidOrdersOlderThan();

        // 주문에서는 쿠폰 금액 차감만 하고 쿠폰 사용저리는 결제때 구현으로 변경
        // 주문상품 별 갯수 조회하여 상품 재고 원복
        for (OrderEntity order : expiredOrders) {
            orderRepository.updateOrderStatus(order.getId(), PaymentStatus.EXPIRED);

            Optional<OrderProductEntity> orderProduct = orderRepository.findByOrderId(order.getId());
            ProductEntity product = productRepository.findById(orderProduct.get().getProductId())
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

            product.updateStock(orderProduct.get().getQuantity());
            productRepository.save(product);
        }
    }
}
