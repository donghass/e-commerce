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

    UserCouponEntity saveAndFlush(UserCouponEntity dummyUserCoupon);

    List<UserCouponWithCouponDto> findByUserCouponList(Long userId);

    void saveAll(List<UserCouponEntity> userCouponDummyList);

    Optional<Object> findByUserIdAndCouponId(Long userId, Long couponId);

    long count(Long couponId);
}
