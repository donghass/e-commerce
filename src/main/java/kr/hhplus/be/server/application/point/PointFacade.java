package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.common.exception.BusinessException;
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
    public void usePoint(Long orderId){
        OrderEntity order = orderService.readOrder(orderId);

        pointService.useAndHistoryPoint(order);

//        포인트 결제완료 후 비동기 이벤트 처리
//        orderService.updateOrderStatus(orderId);

//        주문 상태 변경 후 이벤트처리하도록 수정
//        orderEventPublisher.publishCompleted(order);
    }
}
