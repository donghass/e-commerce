package kr.hhplus.be.server.infra.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.domain.product.BestSellerCacheRepository;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BestSellerCacheRepositoryImpl implements BestSellerCacheRepository {

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
        return List.of(); // or null
    }

    @Override
    public void saveBestSellerList(List<BestSellerEntity> bestSellerList) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(bestSellerList), 25, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
