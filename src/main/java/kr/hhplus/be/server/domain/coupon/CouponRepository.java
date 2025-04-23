package kr.hhplus.be.server.domain.coupon;


import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository {

    Optional<CouponEntity> findById(Long couponId);

    void updateCouponStock(Long couponId, Long toStock);

    CouponEntity save(CouponEntity coupon);

    List<CouponEntity> saveAll(List<CouponEntity> dummyList);

    Map<Object, Object> findAll();

    CouponEntity saveAndFlush(CouponEntity coupon);

    Optional<CouponEntity> findByIdLock(Long couponId);
}
