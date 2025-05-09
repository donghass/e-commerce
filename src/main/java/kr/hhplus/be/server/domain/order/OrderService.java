package kr.hhplus.be.server.domain.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.coupon.CouponApplyResult;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.domain.user.execption.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final ConcurrencyService concurrencyService;
    private final ProductService productService;
    private final CouponService couponService;

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
            products = productService.decreaseStock(sortedItems);
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

    public void updateOrderStatus(Long orderId){
//        orderRepository.updateOrderStatus(orderId, PaymentStatus.PAID);
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.updateStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }
    @Transactional
    public void expireOrder(OrderEntity order, PaymentStatus status) {
        order.updateStatus(status);
        orderRepository.save(order);
    }

}
