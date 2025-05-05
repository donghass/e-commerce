package kr.hhplus.be.server.infra.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.BestSellerRepository;
import kr.hhplus.be.server.domain.product.QBestSellerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BestSellerRepositoryImpl implements BestSellerRepository {
    private final JpaBestSellerRepository jpaBestSellerRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;


    QBestSellerEntity bestSeller = QBestSellerEntity.bestSellerEntity;
    @Override
    public List<BestSellerEntity> findAll() {
        return queryFactory
            .selectFrom(bestSeller)
            .orderBy(bestSeller.sales.desc())  // sales가 많은 순으로 내림차순 정렬
            .fetch();
    }

    @Override
    public void saveAllAndFlush(List<BestSellerEntity> dummyList) {
        jpaBestSellerRepository.saveAllAndFlush(dummyList);
    }
}
