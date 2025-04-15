package kr.hhplus.be.server;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity.Type;
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointTest {
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void readPoint() {
        // Arrange
        Long userId = 1L;

        PointEntity mockPointEntity = PointEntity.save( userId, 1000L); // 생성자 또는 팩토리로 가정

        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(mockPointEntity));

        // Act
        PointResult result = pointService.readPoint(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.balance()).isEqualTo(1000L);
        verify(pointRepository).findByUserId(userId);
    }

    @Test
    void chargePoint_success() {
        // Arrange
        Long userId = 1L;
        Long chargeAmount = 500L;

        // mock PointEntity 설정
        PointEntity mockPointEntity = PointEntity.save(userId, 1000L); // 생성자 또는 팩토리로 가정

        // chargeAmount 만큼 충전될 것임
        PointResult expectedPointDto = new PointResult(userId, 1500L);  // 1000 + 500

        // repository가 반환할 mock 설정
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(mockPointEntity));

        // Act
        PointResult result = pointService.chargePoint(new ChargePointCommand(userId, chargeAmount));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(expectedPointDto.userId());
        assertThat(result.balance()).isEqualTo(expectedPointDto.balance());

        // pointRepository의 findByUserId 메서드가 호출되었는지 검증
        verify(pointRepository).findByUserId(userId);
    }

    @Test
    void chargePoint_fail_notUser() {
        // Arrange
        Long userId = 1L;
        Long chargeAmount = 500L;

        // repository가 빈 Optional을 반환하도록 설정
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class,
            () -> pointService.chargePoint(new ChargePointCommand(userId, chargeAmount)));

        // pointRepository의 findByUserId 메서드가 호출되었는지 검증
        verify(pointRepository).findByUserId(userId);
    }

    @Test
    void UseAndHistoryPoint() {
        // Arrange
        Long userId = 1L;
        Long userBalance = 2000L;  // 사용자의 현재 포인트
        Long orderTotalAmount = 1000L;  // 주문 금액

        Long couponId = 200L;


        OrderEntity mockOrder = OrderEntity.create(userId, couponId, orderTotalAmount);



        PointEntity mockPointEntity = PointEntity.save(userId, 1000L); // 팩토리

        // 포인트 사용 후 잔액 계산: 2000 - 1000 = 1000
        Long remainingBalance = userBalance - orderTotalAmount;

        // PointHistoryEntity 설정
        PointHistoryEntity mockPointHistory = PointHistoryEntity.save(userId, userBalance,orderTotalAmount,
            Type.USE); // 팩토리


        // mock 설정
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(mockPointEntity));
        doNothing().when(pointRepository).usePoint(userId, remainingBalance);
        doNothing().when(pointHistoryRepository).save(any(PointHistoryEntity.class));  // void 메서드에는 doNothing() 사용


        // Act
        pointService.UseAndHistoryPoint(mockOrder, userBalance);

        // Assert
        verify(pointRepository,times(1)).usePoint(userId, remainingBalance);
        verify(pointHistoryRepository,times(1)).save(any(PointHistoryEntity.class));
    }

    @Test
    void chargePoint() {
        // Arrange
        Long userId = 1L;
        Long chargeAmount = 5000L;
        Long beforeBalance = 10000L;
        Long afterBalance = beforeBalance + chargeAmount;

        PointEntity mockPointEntity = PointEntity.save(userId, beforeBalance); // 포인트 팩토리
        PointHistoryEntity mockPointHistory = PointHistoryEntity.save(userId, beforeBalance,chargeAmount,
            Type.CHARGE); // 포인트 히스토리 팩토리

        // Stub: 포인트 조회
        Mockito.when(pointRepository.findByUserId(userId))
            .thenReturn(Optional.of(mockPointEntity));
        // Stub: 포인트 저장은 내부에서 자동 처리되므로 charge 메서드만 검증
        doNothing().when(pointRepository).charge(userId, afterBalance);
        // Stub: 포인트 히스토리 저장
        doNothing().when(pointHistoryRepository).save(any(PointHistoryEntity.class));

        // Act
        PointResult result = pointService.chargePoint(new ChargePointCommand(userId, chargeAmount));

        // Assert (AssertJ)
        assertThat(result)
            .extracting(PointResult::userId, PointResult::balance)
            .containsExactly(userId, afterBalance);

        Mockito.verify(pointRepository).charge(userId, afterBalance);
        Mockito.verify(pointHistoryRepository).save(any(PointHistoryEntity.class));

    }

    @Test // 충전한도 초과
    void charge_fail() {
        // Arrange
        Long userId = 1L;
        Long beforeBalance = 4500000L;
        PointEntity mockPointEntity = PointEntity.save(userId, beforeBalance); // 팩토리

        Long chargeAmount = 600000L;  // 충전할 금액 (총액이 5000000을 초과)

        // Act & Assert
        assertThrows(BusinessException.class, () -> mockPointEntity.charge(chargeAmount));
    }
}