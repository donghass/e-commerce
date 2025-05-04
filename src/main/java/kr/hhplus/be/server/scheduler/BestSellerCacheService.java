package kr.hhplus.be.server.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.application.product.BestSellerResult;
import kr.hhplus.be.server.domain.product.BestSellerReadType;
import kr.hhplus.be.server.domain.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BestSellerCacheService {

    @Autowired
    private ProductService productService; // 인기상품 정보를 제공하는 서비스

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화를 위한 ObjectMapper

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 00시 30분에 캐시를 갱신하는 스케줄러
    @Scheduled(cron = "0 30 0 * * *")  // 매일 00:30시에 실행
    @Transactional
    public void updatePopularProductsCache() {
        // 인기상품 목록 조회
        List<BestSellerResult> bestSellers = productService.bestSellerList(BestSellerReadType.SCHEDULED);

        // 캐시 업데이트
        String cacheKey = "bestSellerList";  // 고정된 캐시 키 사용

        try {
            // BestSellerResult 객체를 JSON 문자열로 직렬화
            String bestSellersJson = objectMapper.writeValueAsString(bestSellers);

            // 캐시 저장, TTL 25시간 설정
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set(cacheKey, bestSellersJson, 25,
                TimeUnit.HOURS); // TTL 25시간 - 스케줄러 실행시 기존 캐시키가 있다면 새 데이터로 덮어씌운다
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Popular products cache updated at 00:30 with 25 hours TTL.");
    }
}