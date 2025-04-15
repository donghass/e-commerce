package kr.hhplus.be.server.api.order;


import kr.hhplus.be.server.application.order.OrderResult;

public record OrderResponse(Long orderId) {

    public static OrderResponse from(OrderResult dto) {
        return new OrderResponse(dto.orderId());
    }

}