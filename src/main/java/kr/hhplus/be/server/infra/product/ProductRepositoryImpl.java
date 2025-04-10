package kr.hhplus.be.server.infra.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때


    @Override
    public Page<ProductQueryDto> findPagedProducts(Pageable pageable) {
        List<ProductQueryDto> content = List.of(); // 비어 있는 리스트라도 사용
        return new PageImpl<>(content, pageable, 0);
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