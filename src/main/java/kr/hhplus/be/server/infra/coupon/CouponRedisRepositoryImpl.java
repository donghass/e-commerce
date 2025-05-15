package kr.hhplus.be.server.infra.coupon;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import kr.hhplus.be.server.domain.coupon.CouponRedisRepository;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponRedisRepositoryImpl implements CouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private String cachedScriptSha;

    public CouponRedisRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void loadScript() {
        System.out.println(">> loadScript called");
        String script =
            "local stock = redis.call(\"LPOP\", KEYS[2]) " +
                "if not stock then return 0 end " +
                "if redis.call(\"SISMEMBER\", KEYS[1], ARGV[1]) == 1 then " +
                "redis.call(\"LPUSH\", KEYS[2], stock) " + // 재고 복구
                "return -1 end " +
                "redis.call(\"SADD\", KEYS[1], ARGV[1]) " +
                "return 1";

        cachedScriptSha = redisTemplate.execute((RedisCallback<String>) connection ->
            connection.scriptLoad(script.getBytes(StandardCharsets.UTF_8))
        );
    }

    public Long tryIssue(String issuedKey, String stockKey, String userId) {
        List<String> keys = List.of(issuedKey, stockKey);
        List<String> args = List.of(userId);
        System.out.println("cachedScriptSha = " + cachedScriptSha);
        return redisTemplate.execute((RedisCallback<Long>) connection ->
            (Long) connection.evalSha(
                cachedScriptSha,
                ReturnType.INTEGER,
                2,
                issuedKey.getBytes(),
                stockKey.getBytes(),
                userId.getBytes()
            )
        );
    }


}