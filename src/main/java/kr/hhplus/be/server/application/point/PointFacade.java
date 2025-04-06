package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointFacade {
    private final PointService pointService;

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
}
