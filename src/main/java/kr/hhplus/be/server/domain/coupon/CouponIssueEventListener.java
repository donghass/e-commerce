package kr.hhplus.be.server.domain.coupon;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.hhplus.be.server.application.coupon.CouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final CouponRedisRepository couponRedisRepository;

    // 비동기 실행
    @Async("taskExecutor") // 반드시 executor 이름 명시!
    @EventListener
    public void handleCouponIssue(CouponIssueEvent event) {
        Long userId = event.userId();
        Long couponId = event.couponId();

        String stockKey = "coupon:stock:" + couponId;
        String issuedKey = "coupon:issued:" + couponId;

        try {
            // DB에서 쿠폰 정보 먼저 조회
            CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 쿠폰 ID"));

            Long result = couponRedisRepository.tryIssue(issuedKey, stockKey, userId.toString());

            if (result == -1) {
                log.warn("중복 발급: userId={}, couponId={}", userId, couponId);
                return;
            }
            if (result == 0) {
                log.warn("재고 부족: userId={}, couponId={}", userId, couponId);
                return;
            }

//            // 중복 발급인지 체크
//            Long added = redisTemplate.opsForSet().add(issuedKey, userId.toString());
//            if (added == null || added == 0) {
//                log.warn("중복 쿠폰 발급 요청: userId={}, couponId={}", userId, couponId);
//                return;
//            }
//
//            // 재고 차감
//            String stockToken = redisTemplate.opsForList().leftPop(stockKey);
//            if (stockToken == null) {
//                redisTemplate.opsForSet().remove(issuedKey, userId.toString());
//                log.warn("재고 소진: userId={}, couponId={}", userId, couponId);
//                return;
//            }

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

            UserCouponEntity userCoupon = UserCouponEntity.save(
                userId, coupon.getName(), coupon.getId(), coupon.getEndDate()
            );
            // 트랜잭션 설정 DB 저장
            couponService.issuedCoupon(userCoupon);
//            userCouponRepository.save(userCoupon);

            log.info("쿠폰 발급 성공: userId={}, couponId={}", userId, couponId);
        } catch (Exception e) {
            log.error("쿠폰 발급 실패: userId={}, couponId={}", userId, couponId, e);
        }
    }
}
