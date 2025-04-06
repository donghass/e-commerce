package kr.hhplus.be.server.api.order;

import jakarta.validation.Valid;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderControllerDocs {
//    @Operation(summary = "상품 주문", description = "상품을 주문합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        // 실제 로직은 생략하고, Mock 주문 ID 생성
        OrderResponse data = new OrderResponse(1L); // mock 주문 ID

        CommonResponse<OrderResponse> response = new CommonResponse<>(
            201,
            "Created",
            "요청이 정상적으로 처리되었습니다.",
            data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
