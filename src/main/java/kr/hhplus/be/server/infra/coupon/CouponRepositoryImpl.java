package kr.hhplus.be.server.infra.coupon;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.QCouponEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final JpaCouponRepository jpaCouponRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QCouponEntity coupon = QCouponEntity.couponEntity;


    @Override
    public Optional<CouponEntity> findById(Long couponId) {
        return jpaCouponRepository.findById(couponId);
    }

    @Override
    public void updateCouponStock(Long couponId, Long toStock) {
        queryFactory.update(coupon).set(coupon.stock,toStock).where(coupon.id.eq(couponId)).execute();
    }

    @Override
    public CouponEntity save(CouponEntity coupon) {

        return jpaCouponRepository.save(coupon);
    }

    @Override
    public List<CouponEntity> saveAll(List<CouponEntity> dummyList) {
        return jpaCouponRepository.saveAll(dummyList);
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

        return jpaCouponRepository.saveAndFlush(coupon);
    }

    // 쿠폰 발급은 api 호출 시작시 쿠폰 테이블에 id로 조회하여 쿠폰 차감 등 발급 로직 진행하므로 일관성을 위해 api 첫 조회쿼리에 lock
    @Override
    public Optional<CouponEntity> findByIdLock(Long couponId) {
        CouponEntity result = queryFactory
            .selectFrom(coupon)
            .where(coupon.id.eq(couponId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 락 설정
            .fetchOne();

        return Optional.ofNullable(result);
    }
}