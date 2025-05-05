package kr.hhplus.be.server.suportAop.spinLock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockManager {

    private final RedissonClient redissonClient;

    public void lock(String lockKey, int maxRetry, long sleepMillis) throws InterruptedException {
        RLock lock = redissonClient.getFairLock(lockKey);   // 선착순이기 때문에 락 요청이 등록된 순서대로 락 걸기
        int attempt = 0;

        while (attempt < maxRetry) {
            boolean locked = lock.tryLock(1, 3, TimeUnit.SECONDS);  // timeout 1초동안 락 풀리기 기다림 , leaseTime 락 자동 해제 시간
            log.info(" 락 획득 시도 - key: {}", lockKey);
            if (locked) {
                log.info(" 락 획득 성공 - key: {}", lockKey);
                return; // 락 획득 성공
            }
            attempt++;
            Thread.sleep(sleepMillis); // 락 실패하면 잠깐 쉬었다가 다시 시도
        }

        throw new IllegalStateException("락 획득 실패: 최대 재시도 초과");
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
