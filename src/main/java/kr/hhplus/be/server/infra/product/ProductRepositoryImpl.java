package kr.hhplus.be.server.infra.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.QProductEntity;
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
    private final EntityManager em;


    QProductEntity product = QProductEntity.productEntity;



    @Override
    public Page<ProductEntity> findPagedProducts(Pageable pageable) {
        List<ProductEntity> results = queryFactory
            .selectFrom(product)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(product.count())
            .from(product)
            .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Optional<ProductEntity> findById(Long id) {

        return jpaProductRepository.findById(id);
    }

    @Override
    public int updateStock(Long productId, Long quantity) {
        long result = queryFactory
            .update(product)
            .set(product.stock, quantity)
            .where(product.id.eq(productId))
            .execute();

        return (int) result;
    }

    @Override
    public void save(ProductEntity product) {
        jpaProductRepository.save(product);
    }

    @Override
    public List<ProductEntity> saveAll(List<ProductEntity> productDummyList) {
        return jpaProductRepository.saveAll(productDummyList);
    }

    @Override
    public ProductEntity saveAndFlush(ProductEntity dummyProduct) {

        return jpaProductRepository.saveAndFlush(dummyProduct);
    }


}