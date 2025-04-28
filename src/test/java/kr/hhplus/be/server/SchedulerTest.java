package kr.hhplus.be.server;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.scheduler.OrderScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @InjectMocks
    private OrderService orderService; // 테스트 대상

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ConcurrencyService concurrencyService;

    @Mock
    private OrderScheduler orderScheduler;

    @Test
    void expiredOrder_rollBack() {
        // Given
        Long orderId = 1L;
        Long productId = 100L;
        Long quantity = 2L;
        Long currentStock = 5L;
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);

        // 주문 엔티티 (만료 예정 상태)
        OrderEntity expiredOrder = OrderEntity.builder()
            .id(orderId)
            .userId(1L)
            .totalAmount(1000L)
            .status(PaymentStatus.EXPIRED)
            .build();

        ReflectionTestUtils.setField(expiredOrder, "id", orderId);

        // 주문 상품
        OrderProductEntity orderProduct = OrderProductEntity.builder()
            .productId(productId)
            .order(expiredOrder)
            .amount(1000L)
            .quantity(quantity)
            .build();

        // 상품
        ProductEntity product = new ProductEntity(productId, "상품A", "옷", 1000L, currentStock,
            LocalDateTime.now(), LocalDateTime.now());

        // Mock 설정
        when(orderRepository.findNotPaidOrdersOlderThan(any())).thenReturn(List.of(expiredOrder));
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(orderProduct));
//      ConcurrencyService mock 세팅
        when(concurrencyService.productDecreaseStock(productId)).thenReturn(product);
        // When
        orderScheduler.checkAndExpireOrders();

        // Then
        verify(orderRepository).updateOrderStatus(orderId, PaymentStatus.EXPIRED); // 상태 변경
        verify(productRepository).save(any(ProductEntity.class)); // 재고 저장

        // 재고가 5 → 7로 복구되었는지 확인
        assertThat(product.getStock()).isEqualTo(currentStock + quantity);
    }
}