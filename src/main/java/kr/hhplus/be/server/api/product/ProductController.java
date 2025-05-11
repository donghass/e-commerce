package kr.hhplus.be.server.api.product;


import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.product.BestSellerResult;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 상품 목록 조회 API
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor    // 생성자 자동 생성
public class ProductController implements ProductControllerDocs {

    private final ProductFacade productFacade;

    // 상품 목록 조회 API
    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductListResponse>>> getProductList(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

//      Page<ProductDto> productDto = productFacade.readProductList();
//      ProductListResponse 에서 productDto 를 ProductListResponse 로 List 로 담아서 반환해줌
//      CommonResponse<Page<ProductListResponse>> response = CommonResponse.success(ResponseCode.SUCCESS, ProductListResponse.from(productDto));
        Page<ProductResult> productDtoPage = productFacade.readProductList(page, size);
        Page<ProductListResponse> responsePage = productDtoPage.map(ProductListResponse::from);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS, responsePage));
//      return ResponseEntity.ok(response);
    }

    // 3일 인기 상품 5개 조회 API
    @GetMapping("/best")
    public ResponseEntity<CommonResponse<List<BestSellerResponse>>> getBestSeller() {
        List<BestSellerResult> bestSeller = productFacade.bestSellerList();

        List<BestSellerResponse> response = bestSeller.stream()
            .map(BestSellerResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS, response));
    }

    // 일간 인기상품조회 API
    @GetMapping("/top")
    public ResponseEntity<CommonResponse<List<ProductListResponse>>> getTopProducts(@RequestParam(defaultValue = "10") int size) {
        List<ProductResult> topProducts = productFacade.getTopProducts(size);

        List<ProductListResponse> response = topProducts.stream()
            .map(ProductListResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS, response));
    }
}
