package kr.hhplus.be.server.api.order;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderControllerDocs {
    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResult orderResult = orderFacade.createOrder(request.toCommand());
        CommonResponse<OrderResponse> response = CommonResponse.success(ResponseCode.SUCCESS, OrderResponse.from(orderResult));

        return ResponseEntity.ok(response);
    }

}
