package kr.hhplus.be.server.domain.order;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import org.springframework.data.repository.query.Param;

public interface OrderRepository {
// save하고 그대로 반환받기
    <T> T save(OrderEntity order);

    Optional<OrderEntity> findById(Long orderId);

    void updateOrderStatus(Long orderId);

    List<OrderEntity> findNotPaidOrdersOlderThan(LocalDateTime expiredOrder);
    int updateStatus(@Param("orderId") Long orderId, @Param("status") PaymentStatus status);

    Optional<OrderProductEntity> findByOrderId(Long orderId);
}
