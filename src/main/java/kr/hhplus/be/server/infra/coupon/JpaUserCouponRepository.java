package kr.hhplus.be.server.infra.coupon;

import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserCouponRepository extends JpaRepository<UserCouponEntity, Long> {

}
