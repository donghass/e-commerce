package kr.hhplus.be.server.api.order;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.order.OrderDto;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor    // 생성자 자동 생성
public class OrderController implements OrderControllerDocs {
//    @Operation(summary = "상품 주문", description = "상품을 주문합니다.")
    private final OrderFacade orderFacade;
    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderDto orderDto = orderFacade.createOrder(request.toCommand());
//      ProductListResponse 에서 productDto 를 ProductListResponse 로 List 로 담아서 반환해줌
        CommonResponse<OrderResponse> response = CommonResponse.success(ResponseCode.SUCCESS, OrderResponse.from(orderDto));

        return ResponseEntity.ok(response);
    }
}
