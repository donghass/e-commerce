package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.suportAop.spinLock.SpinLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    private final ConcurrencyService concurrencyService;

// 쿠폰 사용 // 리팩토링해서 사용 x
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
    @SpinLock(key = "'coupon:' + #command.couponId()")       // @Order(Ordered.HIGHEST_PRECEDENCE) = aop 에 생성
    @Transactional
    public void createCoupon(CouponIssueCommand command) {
//        CouponEntity coupon = couponRepository.findByIdLock(command.couponId())
//            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));

        CouponEntity coupon = concurrencyService.couponDecreaseStock(command.couponId());
//        CouponEntity coupon = couponRepository.findById(command.couponId())
//            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));
        coupon.couponUpdate();
        couponRepository.save(coupon);

        // userCoupon 테이블에서 조회하여 있으면 실패
        userCouponRepository.findByUserIdAndCouponId(command.userId(), command.couponId())
            .ifPresent(c -> { throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED); });

        // 사용자 쿠폰 저장
        UserCouponEntity userCoupon = UserCouponEntity.save(command.userId(),coupon.getName(),coupon.getId(),LocalDateTime.now().plusDays(7));

        userCouponRepository.save(userCoupon);
    }

    public List<UserCouponWithCouponDto> userCouponList(Long userId) {
        List<UserCouponWithCouponDto> userCouponList = userCouponRepository.findByUserCouponList(userId);

        return userCouponList;
    }

    @Transactional
    public void userCouponStatus(Long userCouponId, boolean isUsed) {
            UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

            userCoupon.status(userCoupon, isUsed);

            userCouponRepository.save(userCoupon);
    }
}
