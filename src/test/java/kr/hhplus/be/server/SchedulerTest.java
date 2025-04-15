package kr.hhplus.be.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
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

    @Test
    void expiredOrder_rollBack() {
        // Given
        Long orderId = 1L;
        Long productId = 100L;
        Long quantity = 2L;
        Long currentStock = 5L;

        OrderEntity expiredOrder = new OrderEntity(orderId,1L,1L,1000L,PaymentStatus.EXPIRED,
            LocalDateTime.now(),LocalDateTime.now());
        ReflectionTestUtils.setField(expiredOrder, "id", 1L); // 👈 이게 핵심!

        OrderProductEntity orderProduct = new OrderProductEntity(1L,productId,1L,1000L,quantity,null,null);


        ProductEntity product = new ProductEntity(productId,"상품A","옷",1000L,currentStock, LocalDateTime.now(),LocalDateTime.now());



        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(orderProduct));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        orderService.expireOldUnpaidOrders();

        // Then
        verify(orderRepository).updateStatus(orderId, PaymentStatus.EXPIRED);
        verify(productRepository).updateStock(productId, currentStock + quantity);
    }
}