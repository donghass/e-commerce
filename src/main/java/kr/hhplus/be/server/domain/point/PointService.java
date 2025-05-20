package kr.hhplus.be.server.domain.point;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity.Type;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;


    // 포인트 조회
    public PointResult readPoint(Long userId) {
        PointEntity point = pointRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        return new PointResult(point.getId(),point.getUserId(), point.getBalance());
    }
    // 레디스로 바꾸면 불필요
    // 동시성 충동 발생시 재시도 1회
    public PointResult chargePointWithRetry(ChargePointCommand command) {
        int retry = 3;

        while (retry-- > 0) {
            try {
                return chargePoint(command); // 충전 로직 호출
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (retry == 0) {
                    throw new BusinessException(PointErrorCode.CONFLICT);
                }
                try {
                    Thread.sleep(100); // 잠깐 대기 후 재시도
                } catch (InterruptedException ignored) {}
            }
        }

        throw new BusinessException(PointErrorCode.CONFLICT); // 이건 거의 안 터짐
    }
    // 포인트 충전
    @Transactional
    public PointResult chargePoint(ChargePointCommand command) {
        PointEntity point = pointRepository.findByUserId(command.userId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));


        point.charge(command.amount());
        pointRepository.save(point);

        PointHistoryEntity pointHistory = PointHistoryEntity.save(point.getId(),command.amount(),point.getBalance(),
            Type.CHARGE);

        pointHistoryRepository.save(pointHistory);

        return new PointResult(point.getId(), point.getUserId(), point.getBalance());
    }

    // 포인트 사용
    @Transactional
    public void UseAndHistoryPoint(OrderEntity order){

        PointEntity point = pointRepository.findByUserId(order.getUserId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        point.use(order.getTotalAmount());

        pointRepository.save(point);


        PointHistoryEntity pointHistory = PointHistoryEntity.save(point.getId(), order.getTotalAmount(), point.getBalance(), Type.USE);
        pointHistoryRepository.save(pointHistory);
    }
}
