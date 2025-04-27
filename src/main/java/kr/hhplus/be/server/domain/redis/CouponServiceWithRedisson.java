package kr.hhplus.be.server.domain.redis;

import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.CouponService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponServiceWithRedisson {

    private final RedissonClient redissonClient;
    private final CouponService couponService;

    public void createCoupon(CouponIssueCommand command) {
        String lockKey = "lockCoupon:" + command.couponId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS); 
            // 최대 5초간 기다리고, 락은 3초동안 유지

            if (!locked) {
                throw new RuntimeException("쿠폰 락 획득 실패");
            }

            // 락 잡은 후 쿠폰 발급 로직 실행
            couponService.createCoupon(command);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 대기 중 인터럽트", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
