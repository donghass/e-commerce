package kr.hhplus.be.server.api.product;


import java.util.Arrays;
import java.util.List;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 상품 목록 조회 API
@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductControllerDocs {

    // 상품 목록 조회 API
    @GetMapping
    public ResponseEntity<CommonResponse<List<ProductListResponse>>> getProductList() {
        // 예시 상품 목록
        List<ProductListResponse> products = Arrays.asList(
            new ProductListResponse(1, "Macbook Pro", 2000000, 10),
            new ProductListResponse(2, "iPhone 12", 1200000, 20)
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
