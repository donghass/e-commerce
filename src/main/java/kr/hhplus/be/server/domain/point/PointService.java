package kr.hhplus.be.server.domain.point;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity.Type;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final RestTemplate restTemplate; // 데이터플렛폼 전송 restTemplate

    // 포인트 조회
    public PointResult readPoint(Long userId) {
        PointEntity point = pointRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        return new PointResult(point.getUserId(), point.getBalance());
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

        return new PointResult(point.getUserId(), point.getBalance());
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

    // 데이터플렛폼전송
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 트랜잭션 분리
    public void sendToDataPlatform(OrderEntity order) {
        String url = "https://mock-dataplatform.com/api/payments"; // 가상 플랫폼 URL

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OrderEntity> entity = new HttpEntity<>(order, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("데이터 플랫폼에 성공적으로 전송됨");
        } else {
            System.err.println("전송 실패: " + response.getStatusCode());
        }
    }
}
