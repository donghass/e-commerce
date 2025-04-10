package kr.hhplus.be.server.application.product;

import java.util.List;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductService productService;
    public Page<ProductDto> readProductList(int page, int size) {

        return productService.readProductList(PageRequest.of(page, size));
    }
}
