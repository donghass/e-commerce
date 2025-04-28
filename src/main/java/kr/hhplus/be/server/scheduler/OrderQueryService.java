package kr.hhplus.be.server.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderEntity> findExpiredOrders() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
        return orderRepository.findNotPaidOrdersOlderThan(expiredTime);
    }

    // 조회 트랜잭션 분리
    @Transactional(readOnly = true)
    public OrderProductEntity findByOrder(Long orderId) {
        return orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDERPRODUCT_NOT_FOUND));
    }

}