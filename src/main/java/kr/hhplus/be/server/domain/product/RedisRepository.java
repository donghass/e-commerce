package kr.hhplus.be.server.domain.product;

import java.util.List;
import java.util.Set;

public interface RedisRepository {
    List<BestSellerEntity> getCachedBestSellerList();
    void saveBestSellerList(List<BestSellerEntity> bestSellerList);

    Set<Long> getTopProducts(String key, int topN);

    void increaseScore(String key, Long productId, Long quantity);
}
