package kr.hhplus.be.server.domain.coupon;


import java.util.Optional;

public interface CouponRepository {

    Optional<CouponEntity> findByCouponId(Long couponId);

    void updateCouponStock(Long couponId, Long toStock);
}
