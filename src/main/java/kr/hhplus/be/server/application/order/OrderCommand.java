package kr.hhplus.be.server.application.order;

import java.util.List;

//controller  에서 application 로 받아오는 dto
public record OrderCommand(Long userId, Long userCouponId, List<OrderProduct> orderItem) {
    public record OrderProduct(Long productId,Long quantity) {}
}
