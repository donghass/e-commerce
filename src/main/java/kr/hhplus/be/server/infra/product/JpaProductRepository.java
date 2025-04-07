package kr.hhplus.be.server.infra.product;

import java.util.Optional;
import kr.hhplus.be.server.domain.product.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByUserId(Long userId);
}
