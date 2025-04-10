package kr.hhplus.be.server.domain.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.application.product.ProductDto;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    // 상품 리스트 조회
    public Page<ProductDto> readProductList(Pageable pageable) {
        Page<ProductQueryDto> product = productRepository.findPagedProducts(pageable);

        // 엔티티를 DTO로 변환
//        return product.stream()
//            .map(productDto -> new ProductDto(
//                productDto.getId(),
//                productDto.getName(),
//                productDto.getPrice(),
//                productDto.getStock()))
//            .collect(Collectors.toList());
        return product.map(p -> new ProductDto(
            p.getId(),
            p.getName(),
            p.getPrice(),
            p.getStock()
        ));
    }

    public Long readOrderProduct(List<OrderProduct> orderProduct){
        Long totalAmount = 0L;
        for(int i = 0; i < orderProduct.size(); i++){
            Long productId = orderProduct.get(i).productId();
            Long quantity = orderProduct.get(i).quantity();
            Long updateQuantity;

            // 재고 조회 로직 (예시)
            ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

            Long currentStock = product.getStock();
            if (quantity > currentStock) {
                throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
            }
            // 재고 차감
            updateQuantity = currentStock - quantity;
            productRepository.updateStock(productId, updateQuantity);

            totalAmount += product.getPrice() * quantity;
        }
        // 주문 총액
        return totalAmount;
    }
}
