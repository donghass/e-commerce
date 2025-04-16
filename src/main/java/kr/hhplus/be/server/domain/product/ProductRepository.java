package kr.hhplus.be.server.domain.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository {
    Page<ProductEntity> findPagedProducts(Pageable pageable);
    Optional<ProductEntity> findById(Long Id);
    int updateStock(@Param("productId") Long productId, @Param("stock") Long stock);


    void save(ProductEntity product);


    void saveAll(List<ProductEntity> productDummyList);
}
