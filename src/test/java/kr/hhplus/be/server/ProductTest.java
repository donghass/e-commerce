package kr.hhplus.be.server;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.infra.product.ProductQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void readProductListTest() {
        // Arrange
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        List<ProductQueryDto> content = List.of(
            new ProductQueryDto(1L, "상품A", 1000L, 10L),
            new ProductQueryDto(2L, "상품B", 2000L, 5L)
        );

        Page<ProductQueryDto> queryDtoPage = new PageImpl<>(content, pageable, content.size());

        // Act
        Page<ProductResult> result = productService.readProductList(PageRequest.of(page, size));

        // Assert // 결과 사이즈가 2인지 검증
        assertThat(result).hasSize(2);

        //결과 리스트 안의 각 요소에서 "name"과 "price" 필드만 추출해서
        //두 상품이 정확히 그 순서와 값으로 들어 있는지 검증
        assertThat(result)
            .extracting(ProductResult::name, ProductResult::price)
            .containsExactly(
                tuple("상품A", 1000L),
                tuple("상품B", 2000L)
            );
    }

    @Test
    void readOrder_정상_총합계계산_성공() {
        // Arrange
        List<OrderProduct> orderList = List.of(
            new OrderProduct(1L, 2L), // 상품A: 2개
            new OrderProduct(2L, 1L)  // 상품B: 1개
        );

        // 상품A
        ProductEntity productA = new ProductEntity();
        productA.setId(1L);
        productA.setName("상품A");
        productA.setPrice(1000L);
        productA.setStock(10L);

        // 상품B
        ProductEntity productB = new ProductEntity();
        productB.setId(2L);
        productB.setName("상품B");
        productB.setPrice(2000L);
        productB.setStock(5L);

        // Mock 설정
        when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
        when(productRepository.findById(2L)).thenReturn(Optional.of(productB));

        // Act
        Long result = productService.readOrderProduct(orderList);

        // Assert
        assertThat(result).isEqualTo(2000L + 2000L); // 2*1000 + 1*2000 = 4000
        verify(productRepository).updateStock(1L, 8L); // 10 - 2
        verify(productRepository).updateStock(2L, 4L); // 5 - 1
    }
}