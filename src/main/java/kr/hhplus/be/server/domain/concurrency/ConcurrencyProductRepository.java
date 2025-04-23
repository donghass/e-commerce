package kr.hhplus.be.server.domain.concurrency;

import java.util.Optional;
import kr.hhplus.be.server.domain.product.ProductEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcurrencyProductRepository {

    Optional<ProductEntity> findById(Long productId);
}
