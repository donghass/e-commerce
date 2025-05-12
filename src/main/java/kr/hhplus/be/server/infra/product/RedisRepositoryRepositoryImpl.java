package kr.hhplus.be.server.infra.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.LinkedHashSet;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryRepositoryImpl implements RedisRepository {

    private static final String CACHE_KEY = "bestSellerList";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<BestSellerEntity> getCachedBestSellerList() {
        String cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return List.of();
    }

    @Override
    public void saveBestSellerList(List<BestSellerEntity> bestSellerList) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(bestSellerList), 25, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Long> getTopProducts(String key, int topN) {
        Set<String> result = redisTemplate.opsForZSet().reverseRange(key, 0, topN - 1);
        if (result == null) return Set.of();
        return result.stream().map(Long::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void increaseScore(String key, Long productId, Long quantity) {
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), quantity);
        redisTemplate.expire(key, Duration.ofDays(2)); // 하루 지난 데이터는 자동 만료
    }
}
