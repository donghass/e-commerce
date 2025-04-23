package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.domain.user.execption.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final ConcurrencyService concurrencyService;



    // 주문 : 주문 상태, 토탈 주문 금액 insert
    public Long createOrder(OrderCommand command) {

        // 유저 검증
        UserEntity user = userRepository.findById(command.userId())
            .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_USER_ID));

        UserCouponEntity userCoupon = null;
        CouponEntity coupon = null;
        if (command.userCouponId() != null) {
            userCoupon = userCouponRepository.findById(command.userCouponId())
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

            coupon = couponRepository.findById((userCoupon.getCouponId()))
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        }
        // 도메인 객체 생성
        OrderEntity order = OrderEntity.create(user, userCoupon);
        orderRepository.save(order);
        for (OrderProduct op : command.orderItem()) {
            Long productId = op.productId();
            Long quantity = op.quantity();

            // 상품 조회
//            ProductEntity product = productRepository.findByIdLock(productId)
//                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));
//              ProductEntity product = ConcurrencyRepository.findById(productId);
            ProductEntity product = concurrencyService.productDecreaseStock(productId);

            // 재고 차감
            product.updateStock(quantity);

            productRepository.save(product);

            // 주문 상품 추가 및 주문 총액 계산
            order.addOrderProduct(product, quantity);

            orderRepository.saveAll(order.getOrderItems());
        }
        if(coupon != null) {
            order.discountApply(coupon);
            // 쿠폰 적용
            userCoupon.status(userCoupon, true);
            userCouponRepository.save(userCoupon);
        }

        // 주문 생성
        OrderEntity saved = orderRepository.save(order);

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
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
        List<OrderEntity> expiredOrders = orderRepository.findNotPaidOrdersOlderThan(expiredTime);

        // 주문에서는 쿠폰 금액 차감만 하고 쿠폰 사용처리는 결제때 구현으로 변경
        // 주문상품 별 갯수 조회하여 상품 재고 원복
        for (OrderEntity order : expiredOrders) {
            orderRepository.updateOrderStatus(order.getId(), PaymentStatus.EXPIRED);

            if(order.getUserCouponId() != null) {
                UserCouponEntity userCoupon = userCouponRepository.findById(order.getUserCouponId())
                    .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));
                userCoupon.status(userCoupon, false);

                userCouponRepository.save(userCoupon);
            }

            Optional<OrderProductEntity> orderProduct = orderRepository.findByOrderId(order.getId());
            ProductEntity product = concurrencyService.productDecreaseStock(orderProduct.get().getProductId());

            product.plusStock(orderProduct.get().getQuantity());
            productRepository.save(product);
        }
    }
}
