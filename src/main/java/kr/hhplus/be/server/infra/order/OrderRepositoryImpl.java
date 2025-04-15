package kr.hhplus.be.server.infra.order;

import static com.querydsl.core.types.dsl.Expressions.dateTimeTemplate;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.QOrderEntity;
import kr.hhplus.be.server.domain.order.QOrderProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaProductRepository;
    private final JpaOrderProductRepository jpaProductProductRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QOrderEntity order = QOrderEntity.orderEntity;
    QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;

    @Override
    public <T> T save(OrderEntity order) {
        return null;
    }

    @Override
    public Optional<OrderEntity> findById(Long orderId) {
        return Optional.empty();
    }

    // 결제하여 주문상태값 변경
    @Override
    public void updateOrderStatus(Long orderId, PaymentStatus status) {
        queryFactory.update(order).set(order.status,status).where(order.id.eq(orderId)).execute();
    }

    // createAt이 5분지난 미결재건 주문건 조회
    @Override
    public List<OrderEntity> findNotPaidOrdersOlderThan() {
        List<OrderEntity> expiredOrders = queryFactory
            .selectFrom(order)
            .where(dateTimeTemplate(LocalDateTime.class,
                "DATE_ADD({0}, INTERVAL 5 MINUTE)", order.createdAt
            ).lt(LocalDateTime.now()),order.status.eq(PaymentStatus.NOT_PAID))
            .fetch();
        return List.of();
    }
// 사용안함
    @Override
    public int updateStatus(Long orderId, PaymentStatus status) {
        return 0;
    }

    @Override
    public Optional<OrderProductEntity> findByOrderId(Long orderId) {
        return Optional.empty();
    }

    // insert
    @Override
    public OrderProductEntity orderItemSave(OrderProductEntity orderProduct) {
        em.persist(orderProduct); // INSERT 쿼리 발생
        return orderProduct;
    }
}