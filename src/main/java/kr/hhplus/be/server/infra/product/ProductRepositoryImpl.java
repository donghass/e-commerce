package kr.hhplus.be.server.infra.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때



    @Override
    public List<ProductQueryDto> findAllPoints() {
        return List.of();
    }

    @Override
    public Optional<ProductEntity> findById(Long Id) {
        return Optional.empty();
    }

    @Override
    public int updateStock(Long productId, Long quantity) {
        return 0;
    }


}