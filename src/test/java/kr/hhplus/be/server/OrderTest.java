package kr.hhplus.be.server;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class) // 필수
public class OrderTest {
        @Mock
        private OrderRepository orderRepository;

        @InjectMocks
        private OrderService orderService;

    @Test
    void createOrder_rateDiscount() {
        // Arrange
        Long userId = 1L;
        Long couponId = 100L;
        Long amount = 10000L; // 원래 금액
        int discountRate = 20; // 20% 할인

        OrderCommand command = new OrderCommand(userId, couponId, List.of());
        CouponDiscountResult discount = new CouponDiscountResult((long) discountRate, DiscountType.RATE);

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(123L); // 임의의 저장 결과 ID
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act  실행
        Long result = orderService.createOrder(command, amount, discount);

        // Assert 검증
        assertThat(result).isEqualTo(123L); // 저장된 order ID
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isEqualTo(8000L); // 10000 - 20%
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getUserCouponId()).isEqualTo(couponId);
    }

    @Test
    void createOrder_amountDiscount() {
        // Arrange
        Long userId = 2L;
        Long couponId = 200L;
        Long amount = 10000L; // 원래 금액
        Long discountAmount = 3000L;

        OrderCommand command = new OrderCommand(userId, couponId, List.of());
        CouponDiscountResult discount = new CouponDiscountResult(discountAmount, DiscountType.AMOUNT);

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(456L);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act
        Long result = orderService.createOrder(command, amount, discount);

        // Assert
        assertThat(result).isEqualTo(456L);
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isEqualTo(7000L); // 10000 - 3000
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getUserCouponId()).isEqualTo(couponId);
    }
// 주문 조회
    @Test
    void readOrder_success() {
        // Arrange
        Long orderId = 123L;
        OrderEntity mockOrder = new OrderEntity();
        mockOrder.setId(orderId);
        mockOrder.setUserId(1L);
        mockOrder.setTotalAmount(8000L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        OrderEntity result = orderService.readOrder(orderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualTo(8000L);

        verify(orderRepository).findById(orderId);
    }
// 주문 없음
    @Test
    void readOrder_fail() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> orderService.readOrder(orderId));

        verify(orderRepository).findById(orderId);
    }
    // 주문 상태 변경
    @Test
    void updateOrderStatus() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderRepository).updateOrderStatus(orderId);

        // Act
        orderService.updateOrderStatus(orderId);

        // Assert
        verify(orderRepository, times(1)).updateOrderStatus(orderId);
    }
}
