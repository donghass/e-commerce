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
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final JpaOrderProductRepository jpaOrderProductRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QOrderEntity order = QOrderEntity.orderEntity;
    QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;

    @Override
    public <T> T save(OrderEntity order) {

        return (T) jpaOrderRepository.save(order);
    }

    @Override
    public Optional<OrderEntity> findById(Long id) {

        return jpaOrderRepository.findById(id);
    }

    // 결제하여 주문상태값 변경
    @Override
    public void updateOrderStatus(Long orderId, PaymentStatus status) {
        queryFactory.update(order).set(order.status,status).where(order.id.eq(orderId)).execute();
    }

    // createAt이 5분지난 미결재건 주문건 조회
    @Override
    public List<OrderEntity> findNotPaidOrdersOlderThan(LocalDateTime expiredTime) {
        return queryFactory.selectFrom(order)
            .where(
                order.createdAt.before(LocalDateTime.now().minusMinutes(5)),
                order.status.eq(PaymentStatus.NOT_PAID)
            )
            .fetch();
    }
// 사용안함
//    @Override
//    public int updateStatus(Long orderId, PaymentStatus status) {
//        return 0;
//    }

    @Override
    public Optional<OrderProductEntity> findByOrderId(Long orderId) {
        return jpaOrderProductRepository.findByOrderId(orderId);

    }

    // insert
    @Override
    public OrderProductEntity orderItemSave(OrderProductEntity orderProduct) {
        em.persist(orderProduct); // INSERT 쿼리 발생
        return orderProduct;
    }

    @Override
    public OrderEntity saveAndFlush(OrderEntity dummyOrder) {
        return jpaOrderRepository.saveAndFlush(dummyOrder);
    }

    @Override
    public void saveAll(List<OrderProductEntity> orderItems) {
        jpaOrderProductRepository.saveAll(orderItems);
    }
}