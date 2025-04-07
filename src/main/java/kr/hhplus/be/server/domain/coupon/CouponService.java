package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public Long useCoupon(Long userCouponId) {


        Coupon coupon = couponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

        // 1. 쿠폰 유효성 검증
        if (coupon.isExpiredAt()) {
            throw new BusinessException(CouponErrorCode.COUPON_EXPIRED);
        }
        // boolean 타입
        if (coupon.isUsed()) {
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_USED);
        }

        // 2. 쿠폰 적용 처리 (사용 처리)
        coupon.markAsUsed(); // ex: used = true, usedAt = now
        couponRepository.save(coupon);

        // 3. 할인 금액 반환
        return coupon.getDiscountValue();
    }
}
