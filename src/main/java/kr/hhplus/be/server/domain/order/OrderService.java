package kr.hhplus.be.server.domain.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponApplyResult;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.point.PointEventInfo;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.domain.user.execption.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CouponService couponService;
    private final OrderEventPublisher orderEventPublisher;
    private OrderEntity currentOrder; // 보상용 저장

    // 주문 : 주문 상태, 토탈 주문 금액 insert
    // 재고차감 -> 쿠폰적용 -> 주문 -> 주문상품
    public Long createOrder(OrderCommand command) {
        UserEntity user = userRepository.findById(command.userId())
            .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_USER_ID));

        List<OrderProduct> sortedItems = command.orderItem().stream()
            .sorted(Comparator.comparing(OrderProduct::productId))
            .collect(Collectors.toList());

        List<ProductEntity> products = new ArrayList<>();
        CouponApplyResult couponResult;
        Long orderId;

        try {
            for (OrderProduct op : sortedItems) {
                try {
                    ProductEntity product = productService.decreaseSingleStock(op); // 단건 재고 차감
                    products.add(product);
                } catch (Exception e) {
                    log.error("재고 차감 실패 (productId={})", op.productId(), e);
                    break;
                }
            }
            couponResult = couponService.applyCoupon(command.userCouponId());
            orderId = createOrderAndOrderProduct(user, sortedItems, products, couponResult);
        } catch (Exception e) {
            log.error("주문 실패, 보상 시작", e);
            productService.rollbackStock(sortedItems, products);
            couponService.rollbackCoupon(command.userCouponId());
            rollbackOrder();
            throw new RuntimeException("주문 실패 및 보상 완료", e);
        }
        return orderId;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Long createOrderAndOrderProduct(UserEntity user, List<OrderProduct> items, List<ProductEntity> products, CouponApplyResult couponResult) {
        OrderEntity order = OrderEntity.create(user, couponResult != null ? couponResult.userCoupon() : null);

        for (int i = 0; i < items.size(); i++) {
            log.info("아이템 = "+products.toString());
            order.addOrderProduct(products.get(i), items.get(i).quantity());
        }

        if (couponResult != null) {
            order.discountApply(couponResult.coupon());
        }

        OrderEntity saved = orderRepository.save(order);
        orderRepository.saveAll(order.getOrderProduct());
        this.currentOrder = saved;
        return saved.getId();
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackOrder() {
        if (currentOrder != null) {
            try {
                orderRepository.delete(currentOrder);
            } catch (Exception e) {
                log.error("보상 실패 - 주문 삭제 실패", e);
            }
        }
    }


    // 주문 조회
    public OrderEntity readOrder(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(
            OrderErrorCode.ORDER_NOT_FOUND));
    }

    public void updateOrderStatus(PointEventInfo event){
        OrderEntity order = orderRepository.findById(event.getOrderId())
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        try {
        order.updateStatus(PaymentStatus.PAID);
        orderRepository.save(order);
        } catch (Exception e) {
            // 실패 이벤트 발행
            orderEventPublisher.publishOrderUpdateFailedEvent(event);
            throw e;
        }
        // 데이터플랫폼 전송 비동기 이벤트
        orderEventPublisher.publishCompleted(order);
        // 트랜잭션 커밋 이후 실행되도록 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 주문 상품 목록 기준으로 인기 상품 점수 증가 (커밋 이후 실행)
                for (OrderProductEntity item : order.getOrderProduct()) {
                    Long productId = item.getProductId();
                    Long quantity = item.getQuantity();
                    productService.increaseProductScore(productId, quantity);  // Redis 증가
                }
            }
        });
    }
    @Transactional
    public void expireOrder(OrderEntity order, PaymentStatus status) {
        order.updateStatus(status);
        orderRepository.save(order);
    }
}
