package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
// 쿠폰 사용
    public CouponDiscountResult useCoupon(Long userCouponId) {

        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

        CouponEntity coupon = couponRepository.findByCouponId(userCoupon.getCouponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));

        // 쿠폰 검증
        userCoupon.validateCoupon(userCoupon);

        // 쿠폰 적용 처리 (사용 처리)
        // userCoupon.used = true; // 쿠폰 사용처리는 결제에서 진행

        // 할인 금액, 할인 타입 반환
        return new CouponDiscountResult(coupon.getDiscountValue(),coupon.getDiscountType());
    }

    // 쿠폰발급 동시성 제어는 아직 안함
    @Transactional
    public void createCoupon(CouponIssueCommand command) {
        CouponEntity coupon = couponRepository.findByCouponId(command.couponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));

        // 잔여 수량 없으면 실패
        if(coupon.getStock() <= 0){
            throw new BusinessException(CouponErrorCode.COUPON_OUT_OF_STOCK);
        }

        // userCoupon 테이블에서 조회하여 있으면 실패
        userCouponRepository.findByCouponId(command.couponId())
            .ifPresent(c -> { throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED); });

        // 쿠폰 갯수 차감
        Long toStock = coupon.getStock() - 1;
        couponRepository.updateCouponStock(command.couponId(),toStock);

        // 사용자 쿠폰 저장
        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.setUserId(command.userId());
        userCoupon.setName(coupon.getName());
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setExpiredAt(LocalDateTime.now().plusDays(7));
        userCouponRepository.saveCoupon(userCoupon);
    }

}
