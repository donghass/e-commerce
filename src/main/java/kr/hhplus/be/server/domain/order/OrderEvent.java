package kr.hhplus.be.server.domain.order;
public class OrderEvent {

    public record OrderCompletedEvent(OrderEntity order) {}

    public record OrderUpdateFailedEvent(OrderEntity order) {}
}
