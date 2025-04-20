package kr.hhplus.be.server.domain.order;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {
// save하고 그대로 반환받기
    <T> T save(OrderEntity order);

    Optional<OrderEntity> findById(Long orderId);

    void updateOrderStatus(Long orderId, PaymentStatus status);

    List<OrderEntity> findNotPaidOrdersOlderThan(LocalDateTime expiredTime);

    Optional<OrderProductEntity> findByOrderId(Long orderId);

    OrderProductEntity orderItemSave(OrderProductEntity orderProduct);

    OrderEntity saveAndFlush(OrderEntity dummyOrder);

    void saveAll(List<OrderProductEntity> orderItems);
}
