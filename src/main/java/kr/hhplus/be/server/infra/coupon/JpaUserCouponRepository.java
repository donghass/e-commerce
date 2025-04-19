package kr.hhplus.be.server.infra.coupon;

import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserCouponRepository extends JpaRepository<UserCouponEntity, Long> {

    @Override
    <S extends UserCouponEntity> S save(S entity);

    @Override
    <S extends UserCouponEntity> S saveAndFlush(S entity);

    Optional<UserCouponEntity> findByCouponId(Long couponId);
}
