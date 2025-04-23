package kr.hhplus.be.server.infra.concurrency;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyCouponRepository;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyProductRepository;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.QCouponEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.QProductEntity;
import kr.hhplus.be.server.infra.product.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcurrencyCouponRepositoryImpl implements ConcurrencyCouponRepository {

    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;


    QCouponEntity coupon = QCouponEntity.couponEntity;

    @Override
    public Optional<CouponEntity> findById(Long couponId) {
        CouponEntity result = queryFactory
            .selectFrom(coupon)
            .where(coupon.id.eq(couponId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 락 설정
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
