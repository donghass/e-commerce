package kr.hhplus.be.server.infra.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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


}