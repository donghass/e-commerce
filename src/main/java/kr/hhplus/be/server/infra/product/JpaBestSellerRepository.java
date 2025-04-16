package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.domain.product.BestSellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBestSellerRepository extends JpaRepository<BestSellerEntity, Long> {

}
