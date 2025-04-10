package kr.hhplus.be.server.domain.product;

import java.util.Optional;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface ProductRepository {
    Page<ProductQueryDto> findPagedProducts(Pageable pageable);
    Optional<ProductEntity> findById(Long Id);
    int updateStock(@Param("productId") Long productId, @Param("quantity") Long quantity);


}
