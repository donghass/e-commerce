package kr.hhplus.be.server.order;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.point.PointEventInfo;
import kr.hhplus.be.server.domain.user.UserEntity;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        Long discountRate = 20L; // 20% 할인
        Long totalAmount = 8000L;

        UserEntity user = new UserEntity(userId, LocalDateTime.now(),LocalDateTime.now());
        UserCouponEntity userCoupon = new UserCouponEntity(1L,userId,couponId,false,"쿠폰",null,LocalDateTime.now().plusDays(7),  LocalDateTime.now(),LocalDateTime.now());

        OrderCommand command = new OrderCommand(userId, couponId, List.of());

        CouponDiscountResult discount = new CouponDiscountResult(discountRate, DiscountType.RATE);

        OrderEntity savedOrder = OrderEntity.create(user, userCoupon);
        ReflectionTestUtils.setField(savedOrder, "id", 123L); // 또는 생성자에 id 포함
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act  실행
        Long result = orderService.createOrder(command);

        // Assert 검증
        assertThat(result).isEqualTo(123L); // 저장된 order ID
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity saved = captor.getValue();
        //assertThat(saved.getTotalAmount()).isEqualTo(8000L); // 10000 - 20%
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
        Long totalAmount = 7000L;

        OrderCommand command = new OrderCommand(userId, couponId, List.of());
        CouponDiscountResult discount = new CouponDiscountResult(discountAmount, DiscountType.AMOUNT);

        UserEntity user = new UserEntity(userId, LocalDateTime.now(),LocalDateTime.now());
        UserCouponEntity userCoupon = new UserCouponEntity(1L,userId,couponId,false,"쿠폰",null,LocalDateTime.now().plusDays(7),  LocalDateTime.now(),LocalDateTime.now());


        OrderEntity savedOrder = OrderEntity.create(user, userCoupon);
        ReflectionTestUtils.setField(savedOrder, "id", 123L); // 또는 생성자에 id 포함

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act
        Long result = orderService.createOrder(command);

        // Assert
        assertThat(result).isEqualTo(123L);
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity saved = captor.getValue();
        //assertThat(saved.getTotalAmount()).isEqualTo(7000L); // 10000 - 3000
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getUserCouponId()).isEqualTo(couponId);
    }
// 주문 조회
    @Test
    void readOrder_success() {
        // Arrange
        Long orderId = 123L;
        Long userId = 1L;
        Long couponId = 200L;
        Long totalAmount = 7000L;

        UserEntity user = new UserEntity(userId, LocalDateTime.now(),LocalDateTime.now());
        UserCouponEntity userCoupon = new UserCouponEntity(1L,userId,couponId,false,"쿠폰",null,LocalDateTime.now().plusDays(7),  LocalDateTime.now(),LocalDateTime.now());

        OrderEntity mockOrder = OrderEntity.create(user, userCoupon);
        ReflectionTestUtils.setField(mockOrder, "id", 123L); // 또는 생성자에 id 포함

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        OrderEntity result = orderService.readOrder(orderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualTo(7000L);

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
// 현재 시간에서 10분을 뺀 시간 (10분 전)
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);

        // 준비: 더미 주문 생성
        OrderEntity dummyOrders = Instancio.of(OrderEntity.class)
            .ignore(field(OrderEntity.class, "id"))
            .set(field(OrderEntity.class, "createdAt"), tenMinutesAgo)
            .set(field(OrderEntity.class, "userCouponId"), null)
            .set(field(OrderEntity.class, "userId"), orderId) // userId 설정
            .set(Select.field(OrderEntity.class, "status"), PaymentStatus.NOT_PAID)
            .set(Select.field(OrderEntity.class, "orderProduct"), new ArrayList<>())
            .create();
        // 주문 저장
        OrderEntity savedOrder = orderRepository.saveAndFlush(dummyOrders);

        PointEventInfo pointEventInfo = null;
        pointEventInfo.save(savedOrder,1L);
        doNothing().when(orderRepository).updateOrderStatus(orderId, PaymentStatus.PAID);

        // Act
        orderService.updateOrderStatus(pointEventInfo);

        // Assert
        verify(orderRepository, times(1)).updateOrderStatus(orderId, PaymentStatus.PAID);
    }
}
