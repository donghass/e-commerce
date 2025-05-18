package kr.hhplus.be.server.application.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
@Slf4j
public class OrderFacade {
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;


    // 주문
    public OrderResult createOrder(OrderCommand command) {

        Long orderId = orderService.createOrder(command);
        return new OrderResult(orderId);
    }

    public void expireSingleOrder(OrderEntity order, List<OrderProductEntity> orderProducts) {
        // 재고복원 -> 쿠폰복원 -> 주문상태 복원
        boolean orderExpired = false;
        boolean couponRestored = false;
        List<OrderProductEntity> restoredStock = new ArrayList<>();
        try {
            // 상품 재고 복원 (분산 락 포함, 트랜잭션 분리)
            // 데드락 방지를 위한 오더링
            List<OrderProductEntity> sortedProducts = orderProducts.stream()
                .sorted(Comparator.comparing(OrderProductEntity::getProductId))
                .collect(Collectors.toList());

            for (OrderProductEntity orderProduct : sortedProducts) {
                productService.expireOrder(orderProduct);
                restoredStock.add(orderProduct);
            }
            // 쿠폰 복구
            if (order.getUserCouponId() != null) {
                couponService.userCouponStatus(order.getUserCouponId(), false);
                couponRestored = true;
            }

            // 주문 만료
            orderService.expireOrder(order,PaymentStatus.EXPIRED);
            orderExpired = true;

        } catch (Exception e) { // 보상로직도 실패할 경우를 대비하여 실패내역 별도 테이블에 저장?
            log.error("주문 만료 처리 도중 오류 발생. 보상 로직 실행", e);

            // 보상 순서: 주문 → 쿠폰 → 재고 (역순 처리)
            for (OrderProductEntity p : restoredStock) {
                try {
                    productService.expireFailOrder(p);
                } catch (Exception ex) {
                    log.error("재고 롤백 실패", ex);
                }
            }

            if (couponRestored) {
                try {
                    couponService.userCouponStatus(order.getUserCouponId(), true);  // ex: 상태를 사용으로 다시 변경
                } catch (Exception ex) {
                    log.error("쿠폰 롤백 실패", ex);
                }
            }

            if (orderExpired) {
                try {
                    orderService.expireOrder(order,PaymentStatus.NOT_PAID);  // ex: 상태를 NOT_PAID 등으로 되돌림
                } catch (Exception ex) {
                    log.error("주문 롤백 실패", ex);
                }
            }

            throw new RuntimeException("보상 후 재처리 필요: 주문 ID " + order.getId(), e);
        }
    }
}
