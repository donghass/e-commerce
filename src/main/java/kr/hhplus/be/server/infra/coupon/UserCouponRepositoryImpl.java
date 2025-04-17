package kr.hhplus.be.server.infra.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.QUserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.order.QOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final JpaUserCouponRepository jpaCouponRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QUserCouponEntity userCoupon = QUserCouponEntity.userCouponEntity;

    @Override
    public Optional<UserCouponEntity> findById(Long userCouponId) {
        return Optional.empty();
    }

    @Override
    public Optional<UserCouponEntity> findByCouponId(Long couponId) {
        return Optional.empty();
    }

    @Override
    public void save(UserCouponEntity userCoupon) {

    }


}