package kr.hhplus.be.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        OrderEntity expiredOrder = new OrderEntity();
        expiredOrder.setId(orderId);

        OrderProductEntity orderProduct = new OrderProductEntity();
        orderProduct.setProductId(productId);
        orderProduct.setQuantity(quantity);

        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setStock(currentStock);

        when(orderRepository.findNotPaidOrdersOlderThan(any())).thenReturn(List.of(expiredOrder));
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(orderProduct));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        orderService.expireOldUnpaidOrders();

        // Then
        verify(orderRepository).updateStatus(orderId, PaymentStatus.EXPIRED);
        verify(productRepository).updateStock(productId, currentStock + quantity);
    }
}