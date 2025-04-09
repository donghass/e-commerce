package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.application.point.PointDto;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 포인트 조회
    public PointDto readPoint(Long userId) {
        PointEntity point = pointRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        return new PointDto(point.getUserId(), point.getBalance());
    }
    // 포인트 충전
    @Transactional  // JPA 영속성 때문에 save 하지 않아도 자동으로 충전금액 update 된다 // 신규 데이터 추가일 경우엔 안됨
    public PointDto chargePoint(ChargePointCommand command) {
        PointEntity point = pointRepository.findByUserId(command.userId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        // 정책 검증 Entity 에서
//        if(point.getBalance() + command.amount() > 5000000){throw new BusinessException(PointErrorCode.EXCEED_TOTAL_CHARGE_LIMIT);}

        point.charge(command.amount());

        return new PointDto(point.getUserId(), point.getBalance());
    }

    // 포인트 사용
    public void UseAndHistoryPoint(OrderEntity order,Long userBalance){
        Long balance = userBalance - order.getTotalAmount(); // 사용 후 금액 = 현재 사용자 포인트 - 주문 금액
        if(balance <= 0){
            throw new BusinessException(PointErrorCode.POINT_BALANCE_INSUFFICIENT);
        }
        pointRepository.usePoint(order.getUserId(), balance);

        PointEntity point = pointRepository.findByUserId(order.getUserId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        PointHistoryEntity pointHistory = new PointHistoryEntity();
        pointHistory.setPointId(point.getId());
        pointHistory.setBalance(balance);
        pointHistory.setAmount(order.getTotalAmount());
        pointHistoryRepository.save(pointHistory);
    }


}
