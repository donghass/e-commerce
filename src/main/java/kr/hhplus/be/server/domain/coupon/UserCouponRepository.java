package kr.hhplus.be.server.domain.coupon;


import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository {

    Optional<UserCouponEntity> findById(Long userCouponId);

    Optional<UserCouponEntity> findByCouponId(Long couponId);

    void save(UserCouponEntity userCoupon);

    List<UserCouponEntity> findByUserId(long userId);
}
