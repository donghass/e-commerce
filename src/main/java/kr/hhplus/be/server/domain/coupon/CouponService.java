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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    private final ConcurrencyService concurrencyService;

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

    @Cacheable(value = "userCoupons", key = "'userCoupon:' + #userId", unless = "#result == null")
    public List<UserCouponWithCouponDto> userCouponList(Long userId) {
        List<UserCouponWithCouponDto> userCouponList = userCouponRepository.findByUserCouponList(userId);

        return userCouponList;
    }

    //주문 상태 변경(결재,주문시간만료 등) 캐시 적용
    @CacheEvict(value = "userCoupons", key = "'userCoupon:' + #userId")
    @Transactional
    public void userCouponStatus(Long userCouponId, boolean isUsed) {
            UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

            userCoupon.status(userCoupon, isUsed);

            userCouponRepository.save(userCoupon);
    }


    //주문 생성 쿠폰 적용
    @CacheEvict(value = "userCoupons", key = "'userCoupon:' + #userId")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CouponApplyResult applyCoupon(Long userCouponId) {
        if (userCouponId == null) return null;

        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_OWNED));

        CouponEntity coupon = couponRepository.findById(userCoupon.getCouponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));

        userCoupon.status(userCoupon, true);
        userCouponRepository.save(userCoupon);

        return new CouponApplyResult(userCoupon, coupon);
    }
    //주문 생성 쿠폰 보상로직
    @CacheEvict(value = "userCoupons", key = "'userCoupon:' + #userId")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackCoupon(Long userCouponId) {
        if (userCouponId == null) return;
        try {
            UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow();
            userCoupon.status(userCoupon, false);
            userCouponRepository.save(userCoupon);
        } catch (Exception e) {
            log.error("보상 실패 - 쿠폰 복구 실패", e);
        }
    }
}
