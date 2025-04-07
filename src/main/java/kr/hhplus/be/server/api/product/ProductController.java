package kr.hhplus.be.server.api.product;


import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.product.ProductDto;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 상품 목록 조회 API
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor    // 생성자 자동 생성
public class ProductController implements ProductControllerDocs {

    private final ProductFacade productFacade;

    // 상품 목록 조회 API
    @GetMapping
    public ResponseEntity<CommonResponse<List<ProductListResponse>>> getProductList() {

        List<ProductDto> productDto = productFacade.readProductList();
//      ProductListResponse 에서 productDto 를 ProductListResponse 로 List 로 담아서 반환해줌
        CommonResponse<List<ProductListResponse>> response = CommonResponse.success(ResponseCode.SUCCESS, ProductListResponse.from(productDto));

        return ResponseEntity.ok(response);
    }
}
