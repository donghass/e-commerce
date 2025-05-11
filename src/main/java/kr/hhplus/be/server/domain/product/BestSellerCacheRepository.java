package kr.hhplus.be.server.domain.product;

import java.util.List;

public interface BestSellerCacheRepository {
    List<BestSellerEntity> getCachedBestSellerList();
    void saveBestSellerList(List<BestSellerEntity> bestSellerList);
}
