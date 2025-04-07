package kr.hhplus.be.server.domain.product;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import org.springframework.data.repository.query.Param;

public interface ProductRepository {
    List<ProductQueryDto> findAllPoints();
    Optional<ProductEntity> findById(Long Id);
    int updateStock(@Param("productId") Long productId, @Param("quantity") Long quantity);


}
