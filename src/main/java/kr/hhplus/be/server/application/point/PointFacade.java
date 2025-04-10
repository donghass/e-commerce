package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointFacade {
    private final PointService pointService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;

    public PointDto readPoint(Long userId) {
        if (userId <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }
        return pointService.readPoint(userId);
    }

    public PointDto chargePoint(ChargePointCommand command){
        if (command.userId() <= 0) {throw new BusinessException(PointErrorCode.INVALID_USER_ID);}
        if (command.amount() <= 0) {throw new BusinessException(PointErrorCode.INVALID_CHARGE_AMOUNT);}
        if (command.amount() > 1000000) {throw new BusinessException(PointErrorCode.EXCEED_ONE_TIME_LIMIT);}

        return pointService.chargePoint(command);
    }

    // 결재
    @Transactional
    public void usePoint(Long orderId){
        OrderEntity order = orderService.readOrder(orderId);
        PointDto point = pointService.readPoint(order.getUserId());

        pointService.UseAndHistoryPoint(order,point.balance());

        orderService.updateOrderStatus(orderId);

        // 외부 데이터 플랫폼에 전송  -- 트랜잭션 분리
        pointService.sendToDataPlatform(order);
    }
}
