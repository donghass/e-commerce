package kr.hhplus.be.server.domain.coupon;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository {

    Optional<CouponEntity> findByCouponId(Long couponId);

    void updateCouponStock(Long couponId, Long toStock);

    void save(CouponEntity coupon);

    void saveAll(List<CouponEntity> dummyList);

    Map<Object, Object> findAll();

    CouponEntity saveAndFlush(CouponEntity coupon);
}
