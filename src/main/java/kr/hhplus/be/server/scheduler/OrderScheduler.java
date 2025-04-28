package kr.hhplus.be.server.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import kr.hhplus.be.server.domain.redis.OrderServiceWithRedisson;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;
    private final OrderServiceWithRedisson orderServiceWithRedisson;


    // 매 5분마다 실행
    @Scheduled(fixedRate = 300_000)
    public void checkAndExpireOrders() {
//        orderService.expireOldUnpaidOrders();
//        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
        List<OrderEntity> expiredOrders = orderQueryService.findExpiredOrders();
//            orderRepository.findNotPaidOrdersOlderThan(expiredTime);

        // 주문에서는 쿠폰 금액 차감만 하고 쿠폰 사용처리는 결제때 구현으로 변경
        // 주문상품 별 갯수 조회하여 상품 재고 원복
        for (OrderEntity order : expiredOrders) {
//            orderRepository.updateOrderStatus(order.getId(), PaymentStatus.EXPIRED);
            OrderProductEntity orderProduct = orderQueryService.findByOrder(order.getId());
//                orderRepository.findByOrderId(order.getId())
//                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDERPRODUCT_NOT_FOUND));
//          락 걸기
            orderServiceWithRedisson.expireSingleOrder(order,orderProduct);

        }
    }

    // 조회 트랜잭션 분리
    @Transactional(readOnly = true)
    private OrderProductEntity findByOrder(Long orderId) {
        return orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDERPRODUCT_NOT_FOUND));
    }

    // 조회 트랜잭션 분리
    @Transactional(readOnly = true)
    public List<OrderEntity> findExpiredOrders() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
        return orderRepository.findNotPaidOrdersOlderThan(expiredTime);
    }
}