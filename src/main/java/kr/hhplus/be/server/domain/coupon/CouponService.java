package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    @Autowired
    private final CouponRepository couponRepository;
    @Autowired
    private final UserCouponRepository userCouponRepository;
// 쿠폰 사용
    public CouponDiscountResult useCoupon(Long userCouponId) {

        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

        CouponEntity coupon = couponRepository.findById(userCoupon.getCouponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));

        // 쿠폰 검증
        userCoupon.validateCoupon(userCoupon);

        // 할인 금액, 할인 타입 반환
        return new CouponDiscountResult(coupon.getDiscountValue(),coupon.getDiscountType());
    }

    // couponRepository.findById(쿠폰 조회시점에) 에 비관적 락 걸어서 읽기,쓰기 일관성
    @Transactional
    public void createCoupon(CouponIssueCommand command) {
        CouponEntity coupon = couponRepository.findById(command.couponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));

        coupon.couponUpdate();

        couponRepository.save(coupon);

        // userCoupon 테이블에서 조회하여 있으면 실패
        userCouponRepository.findByCouponId(command.couponId())
            .ifPresent(c -> { throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED); });

        // 사용자 쿠폰 저장
        UserCouponEntity userCoupon = UserCouponEntity.save(command.userId(),coupon.getName(),coupon.getId(),LocalDateTime.now().plusDays(7));

        userCouponRepository.save(userCoupon);
    }

}
