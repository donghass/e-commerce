package kr.hhplus.be.server.domain.product;

import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.point.PointDto;
import kr.hhplus.be.server.application.product.ProductDto;
import kr.hhplus.be.server.infra.point.PointQueryDto;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    // 상품 리스트 조회
    public List<ProductDto> readProductList() {
        List<ProductQueryDto> product = productRepository.findAllPoints();



        // 엔티티를 DTO로 변환
        return product.stream()
            .map(productDto -> new ProductDto(
                productDto.getId(),
                productDto.getName(),
                productDto.getPrice(),
                productDto.getStock()))
            .collect(Collectors.toList());

    }
}
