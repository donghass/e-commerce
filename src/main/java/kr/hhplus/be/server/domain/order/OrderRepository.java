package kr.hhplus.be.server.domain.order;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
// save하고 그대로 반환받기
    <T> T save(OrderEntity order);

    Optional<OrderEntity> findById(Long orderId);

    void updateOrderStatus(Long orderId);

    List<OrderEntity> findUnpaidOrdersOlderThan(LocalDateTime expiredOrder);
}
