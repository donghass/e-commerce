package kr.hhplus.be.server.domain.coupon;

import java.time.Duration;
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
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;


    @SpinLock(key = "'coupon:' + #command.couponId()")       // @Order(Ordered.HIGHEST_PRECEDENCE) = aop 에 생성
    @Transactional
    public void createCoupon(CouponIssueCommand command) {

        CouponEntity coupon = concurrencyService.couponDecreaseStock(command.couponId());
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

    // 쿠폰 발급
    @Transactional
    public void issuedCoupon(UserCouponEntity userCoupon) {
        userCouponRepository.save(userCoupon);
    }

    @Transactional  // kafka 쿠폰 발급
    public void couponIssued(CouponIssueCommand command) {
        Long couponId = command.couponId();

        String stockKey = "coupon:stock:" + couponId;
        String issuedKey = "coupon:issued:" + couponId;


        // 1. 재고 감소
        CouponEntity coupon = couponRepository.findById(command.couponId())
            .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));

        // TTL 계산 (현재 시각과 쿠폰 종료일의 차이) - ttl 은 쿠폰의 enddate 로 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = coupon.getEndDate();
        Duration ttl = Duration.between(now, endDate);

        // 쿠폰 만료일이 현시점 과거가 아니고 TTL 이 설정되지 않았을 경우에만 설정
        if (!ttl.isNegative() && !ttl.isZero()) {
            if (redisTemplate.getExpire(stockKey) == -1) {
                redisTemplate.expire(stockKey, ttl);
            }
            if (redisTemplate.getExpire(issuedKey) == -1) {
                redisTemplate.expire(issuedKey, ttl);
            }
        }

        coupon.couponUpdate();
        couponRepository.save(coupon);

        // userCoupon 테이블에서 조회하여 있으면 실패
        userCouponRepository.findByUserIdAndCouponId(command.userId(), command.couponId())
            .ifPresent(c -> { throw new BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED); });

        // 사용자 쿠폰 저장
        UserCouponEntity userCoupon = UserCouponEntity.save(command.userId(),coupon.getName(),coupon.getId(),LocalDateTime.now().plusDays(7));

        userCouponRepository.save(userCoupon);
        log.info("쿠폰 발행 메시지 컨슈머 성공");
    }
}
