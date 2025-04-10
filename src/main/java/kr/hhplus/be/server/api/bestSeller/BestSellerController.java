package kr.hhplus.be.server.api.bestSeller;

import java.util.Arrays;
import java.util.List;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.api.product.ProductListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bestProducts")
public class BestSellerController {
    // 최근 3일간 가장 많이 팔린 인기 상품 5개 조회 API
    //@GetMapping("/best")
    //public ResponseEntity<CommonResponse<List<ProductListResponse>>> getPopularProducts() {


        //return ResponseEntity.ok(response);
//    }
}
