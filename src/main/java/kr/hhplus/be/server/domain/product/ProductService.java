package kr.hhplus.be.server.domain.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    
    public Page<ProductResult> readProductList(Pageable pageable) {
        Page<ProductQueryDto> product = productRepository.findPagedProducts(pageable);


        return product.map(p -> new ProductResult(
            p.getId(),
            p.getName(),
            p.getPrice(),
            p.getStock()
        ));
    }

    @Transactional
    public Long readOrderProduct(List<OrderProduct> orderProduct){
        Long totalAmount = 0L;

        for(int i = 0; i < orderProduct.size(); i++){
            Long productId = orderProduct.get(i).productId();
            Long quantity = orderProduct.get(i).quantity();

            // 재고 조회 로직 (예시)
            ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

            // jpa 쓰면 더티체킹 해서 안써도 되긴하지만! 쓰는게 mybatis 같은 orm 교체시 해줘야하잖아요?
            // 그래서 저는 이렇게 save해주는게 맏가 생각해요 ㅎ
            productRepository.save(product);

            totalAmount += product.getPrice() * quantity;
        }

        return totalAmount;
    }
}
