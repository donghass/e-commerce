package kr.hhplus.be.server.suportAop.redissonLock;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PubSubLockManager {

    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    public boolean tryLock(String key, long timeoutMs, long leaseTimeSeconds) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "locked", Duration.ofSeconds(10));   //key 가 없으면 10초동안 키 저장
            if (success) {
                return true;
            }

            CountDownLatch latch = new CountDownLatch(1);
            String channel = "unlock:" + key;

            MessageListener listener = (message, pattern) -> latch.countDown();
            listenerContainer.addMessageListener(listener, new ChannelTopic(channel));

            latch.await(200, TimeUnit.MILLISECONDS); // 잠깐 대기
            listenerContainer.removeMessageListener(listener);
        }

        return false;
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
        redisTemplate.convertAndSend("unlock:" + key, "released");
    }
}