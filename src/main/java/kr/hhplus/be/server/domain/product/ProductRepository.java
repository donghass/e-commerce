package kr.hhplus.be.server.domain.product;

import java.util.List;
import kr.hhplus.be.server.infra.product.ProductQueryDto;

public interface ProductRepository {
    List<ProductQueryDto> findAllPoints();

}
