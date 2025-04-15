package kr.hhplus.be.server.infra.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.QCouponEntity;
import kr.hhplus.be.server.domain.order.QOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JpaCouponRepository jpaCouponRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QCouponEntity coupon = QCouponEntity.couponEntity;

    @Override
    public Optional<CouponEntity> findByCouponId(Long couponId) {
        return Optional.empty();
    }

    @Override
    public void updateCouponStock(Long couponId, Long toStock) {
        queryFactory.update(coupon).set(coupon.stock,toStock).where(coupon.id.eq(couponId)).execute();
    }

    @Override
    public void save(CouponEntity coupon) {

    }
}