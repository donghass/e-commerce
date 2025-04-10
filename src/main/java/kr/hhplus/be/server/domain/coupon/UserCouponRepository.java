package kr.hhplus.be.server.domain.coupon;


import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCouponEntity> findById(Long userCouponId);

    Optional<UserCouponEntity> findByCouponId(Long couponId);

    void saveCoupon(UserCouponEntity userCoupon);
}
