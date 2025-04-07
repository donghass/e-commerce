package kr.hhplus.be.server.api.order;


import kr.hhplus.be.server.application.order.OrderDto;

public record OrderResponse(Long orderId) {

    public static OrderResponse from(OrderDto dto) {
        return new OrderResponse(dto.orderId());
    }

}