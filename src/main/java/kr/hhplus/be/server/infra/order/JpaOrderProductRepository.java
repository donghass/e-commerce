package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderProductRepository extends JpaRepository<OrderProductEntity, Long> {

}
