package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.OrderEntity;

public record OrderCompletedEvent(OrderEntity order) {}