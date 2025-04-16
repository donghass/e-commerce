package kr.hhplus.be.server.infra.coupon;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.QCouponEntity;
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

    @Override
    public void saveAll(List<CouponEntity> dummyList) {

    }

    @Override
    public Map<Object, Object> findAll() {
        QCouponEntity coupon = QCouponEntity.couponEntity;

        List<Tuple> results = queryFactory
            .select(coupon.id, coupon.name)
            .from(coupon)
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(coupon.id),
                tuple -> tuple.get(coupon.name)
            ));
    }

    @Override
    public CouponEntity saveAndFlush(CouponEntity coupon) {

        return coupon;
    }
}