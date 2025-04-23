package kr.hhplus.be.server.infra.coupon;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.QCouponEntity;
import kr.hhplus.be.server.domain.coupon.QUserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final JpaUserCouponRepository jpaUserCouponRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QUserCouponEntity userCoupon = QUserCouponEntity.userCouponEntity;
    QCouponEntity coupon = QCouponEntity.couponEntity;

    @Override
    public Optional<UserCouponEntity> findById(Long userCouponId) {
        return jpaUserCouponRepository.findById(userCouponId);
    }

    @Override
    public Optional<UserCouponEntity> findByCouponId(Long couponId) {
        return jpaUserCouponRepository.findByCouponId(couponId);
    }

    @Override
    public void save(UserCouponEntity userCoupon) {
        jpaUserCouponRepository.save(userCoupon);
    }

    @Override
    public List<UserCouponEntity> findByUserId(long userId) {
        return (List<UserCouponEntity>) queryFactory
            .selectFrom(userCoupon)
            .where(userCoupon.id.eq(userId))
            .fetchOne();
    }


    @Override
    public UserCouponEntity saveAndFlush(UserCouponEntity dummyUserCoupon) {
        return jpaUserCouponRepository.saveAndFlush(dummyUserCoupon);
    }

    @Override
    public List<UserCouponWithCouponDto> findByUserCouponList(Long userId) {
        return queryFactory
            .select(Projections.constructor(UserCouponWithCouponDto.class,
                userCoupon.id,           // userCouponId
                userCoupon.isUsed,       // isUsed
                userCoupon.expiredAt,  // expiredAt
                coupon.id,           // couponId
                coupon.name,        // name
                coupon.discountType, // discountType (Enum → String)
                coupon.discountValue // discountValue
            ))
            .from(userCoupon)
            .join(coupon).on(userCoupon.couponId.eq(coupon.id))
            .where(userCoupon.userId.eq(userId))
            .fetch();
    }

    @Override
    public void saveAll(List<UserCouponEntity> userCouponDummyList) {
        jpaUserCouponRepository.saveAll(userCouponDummyList);
    }

    @Override
    public Optional<Object> findByUserIdAndCouponId(Long userId, Long couponId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(userCoupon)
                .where(
                    userCoupon.userId.eq(userId),
                    userCoupon.couponId.eq(couponId)
                )
                .fetchOne()
        );
    }
}