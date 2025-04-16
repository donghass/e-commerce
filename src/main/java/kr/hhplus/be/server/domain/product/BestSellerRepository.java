package kr.hhplus.be.server.domain.product;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BestSellerRepository {

    List<BestSellerEntity> findAll();

    void saveAll(List<BestSellerEntity> dummyList);
}
