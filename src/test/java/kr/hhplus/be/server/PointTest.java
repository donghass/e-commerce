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
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        PointEntity mockPointEntity = new PointEntity();
        mockPointEntity.setUserId(userId);
        mockPointEntity.setBalance(1000L);

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
        PointEntity mockPointEntity = new PointEntity();
        mockPointEntity.setUserId(userId);
        mockPointEntity.setBalance(1000L);

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

        OrderEntity mockOrder = new OrderEntity();
        mockOrder.setUserId(userId);
        mockOrder.setTotalAmount(orderTotalAmount);

        PointEntity mockPointEntity = new PointEntity();
        mockPointEntity.setId(1L);
        mockPointEntity.setUserId(userId);
        mockPointEntity.setBalance(userBalance);

        // 포인트 사용 후 잔액 계산: 2000 - 1000 = 1000
        Long remainingBalance = userBalance - orderTotalAmount;

        // PointHistoryEntity 설정
        PointHistoryEntity mockPointHistory = new PointHistoryEntity();
        mockPointHistory.setPointId(mockPointEntity.getId());
        mockPointHistory.setBalance(remainingBalance);
        mockPointHistory.setAmount(orderTotalAmount);

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
    void charge() {
        // Arrange
        PointEntity pointEntity = new PointEntity();
        pointEntity.setBalance(1000L);  // 기존 잔액 설정

        Long chargeAmount = 500L;  // 충전할 금액

        // Act
        pointEntity.charge(chargeAmount);

        // Assert
        assertThat(pointEntity.getBalance()).isEqualTo(1500L);  // 1000 + 500 = 1500
    }

    @Test // 충전한도 초과
    void charge_fail() {
        // Arrange
        PointEntity pointEntity = new PointEntity();
        pointEntity.setBalance(4500000L);  // 기존 잔액 설정 (4500000L)

        Long chargeAmount = 600000L;  // 충전할 금액 (총액이 5000000을 초과)

        // Act & Assert
        assertThrows(BusinessException.class, () -> pointEntity.charge(chargeAmount));
    }
}