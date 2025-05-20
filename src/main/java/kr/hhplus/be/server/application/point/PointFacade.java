package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.DataPlatformClient;
import kr.hhplus.be.server.domain.order.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointFacade {
    private final PointService pointService;
    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;

    public PointResult readPoint(Long userId) {
        if (userId <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);
        }
        return pointService.readPoint(userId);
    }

    public PointResult chargePoint(ChargePointCommand command){
        if (command.userId() <= 0) {throw new BusinessException(PointErrorCode.INVALID_USER_ID);}
        if (command.amount() <= 0) {throw new BusinessException(PointErrorCode.INVALID_CHARGE_AMOUNT);}
        if (command.amount() > 1000000) {throw new BusinessException(PointErrorCode.EXCEED_ONE_TIME_LIMIT);}
        PointResult point = pointService.readPoint(command.userId());
        return pointService.chargePoint(command);
//        return pointServiceWithRedisson.chargePoint(command,point.id());
    }

    // 결재
    @Transactional
    public void usePoint(Long orderId){
        OrderEntity order = orderService.readOrder(orderId);

        pointService.UseAndHistoryPoint(order);

        orderService.updateOrderStatus(orderId);

        // 외부 데이터 플랫폼에 주문 정보 전송  -- 트랜잭션 분리
//        pointService.sendToDataPlatform(order);
        // 커밋 이후 처리할 이벤트 발행
//        applicationEventPublisher.publishEvent(new OrderCompletedEvent(order));
        orderEventPublisher.publishCompleted(order);
    }
}
