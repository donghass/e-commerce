package kr.hhplus.be.server.domain.concurrency;

import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcurrencyCouponRepository {

    Optional<CouponEntity> findById(Long couponId);
}
