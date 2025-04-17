package kr.hhplus.be.server.domain.point;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PointService {
    @Autowired
    private final PointRepository pointRepository;
    @Autowired
    private final PointHistoryRepository pointHistoryRepository;
    @Autowired
    private final RestTemplate restTemplate; // 데이터플렛폼 전송 restTemplate

    // 포인트 조회
    public PointResult readPoint(Long userId) {
        PointEntity point = pointRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        return new PointResult(point.getUserId(), point.getBalance());
    }
    // 포인트 충전
    @Transactional  // JPA 영속성 때문에 save 하지 않아도 자동으로 충전금액 update 된다(더티체킹) // 신규 데이터 추가일 경우엔 안됨
    public PointResult chargePoint(ChargePointCommand command) {
        PointEntity point = pointRepository.findByUserId(command.userId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        // 정책 검증 Entity 에서
//        if(point.getBalance() + command.amount() > 5000000){throw new BusinessException(PointErrorCode.EXCEED_TOTAL_CHARGE_LIMIT);}
        Long chargePoint = point.charge(command.amount());
        pointRepository.charge(command.userId(),chargePoint);

        PointHistoryEntity pointHistory = PointHistoryEntity.save(point.getId(),command.amount(),point.getBalance(),
            Type.CHARGE);

        pointHistoryRepository.save(pointHistory);

        return new PointResult(point.getUserId(), point.getBalance());
    }

    // 포인트 사용
    @Transactional
    public void UseAndHistoryPoint(OrderEntity order,Long userBalance){
        Long balance = userBalance - order.getTotalAmount(); // 사용 후 금액 = 현재 사용자 포인트 - 주문 금액
        if(balance <= 0){
            throw new BusinessException(PointErrorCode.POINT_BALANCE_INSUFFICIENT);
        }
        pointRepository.usePoint(order.getUserId(), balance);

        PointEntity point = pointRepository.findByUserId(order.getUserId())
            .orElseThrow(() -> new BusinessException(PointErrorCode.INVALID_USER_ID));

        PointHistoryEntity pointHistory = PointHistoryEntity.save(point.getId(),order.getTotalAmount(),balance,
            Type.USE);
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
