package kr.hhplus.be.server.application.product;

import java.util.List;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    public List<ProductDto> readProductList() {

        return productService.readProductList();
    }
}
