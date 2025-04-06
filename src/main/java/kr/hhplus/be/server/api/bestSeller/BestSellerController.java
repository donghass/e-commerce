package kr.hhplus.be.server.api.bestSeller;

import java.util.Arrays;
import java.util.List;
import kr.hhplus.be.server.api.CommonResponse;
import kr.hhplus.be.server.api.product.ProductListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bestProducts")
public class BestSellerController {
    // 최근 3일간 가장 많이 팔린 인기 상품 5개 조회 API
    @GetMapping("/best")
    public ResponseEntity<CommonResponse<List<ProductListResponse>>> getPopularProducts() {
        // 최근 3일간 판매된 인기 상품 목록
        List<ProductListResponse> products = Arrays.asList(
            new ProductListResponse(1, "Ice Americano", 1000, 100),
            new ProductListResponse(2, "iPhone 12", 1200000, 90),
            new ProductListResponse(3, "Samsung Galaxy S21", 800000, 70),
            new ProductListResponse(4, "Macbook Air", 1500000, 60),
            new ProductListResponse(5, "Apple Watch", 500000, 50)
        );
        CommonResponse<List<ProductListResponse>> response = new CommonResponse<>(
            200,
            "OK",
            "요청이 정상적으로 처리되었습니다.",
            products
        );

        return ResponseEntity.ok(response);
    }
}
