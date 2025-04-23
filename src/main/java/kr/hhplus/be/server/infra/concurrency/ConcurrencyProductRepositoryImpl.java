package kr.hhplus.be.server.infra.concurrency;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyProductRepository;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.QProductEntity;
import kr.hhplus.be.server.infra.product.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcurrencyProductRepositoryImpl implements ConcurrencyProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;


    QProductEntity product = QProductEntity.productEntity;

    @Override
    public Optional<ProductEntity> findById(Long productId) {
        ProductEntity result = queryFactory
            .selectFrom(product)
            .where(product.id.eq(productId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 락 설정
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
