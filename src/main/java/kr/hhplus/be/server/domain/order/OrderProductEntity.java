package kr.hhplus.be.server.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Table(name="order_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
public class OrderProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @Column(nullable = false, name = "productId")
    private Long productId;
    @Column(nullable = false, name = "ordersId")
    private Long orderId;
    @Column(nullable = false, name = "amount")
    private Long amount;
    @Column(nullable = false, name = "quantity")
    private Long quantity;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static OrderProductEntity create(Long productId, Long orderId, Long amount, Long quantity) {
        OrderProductEntity orderItem = new OrderProductEntity();
        orderItem.productId = productId;
        orderItem.orderId = orderId;
        orderItem.amount = amount;
        orderItem.quantity = quantity;
        return orderItem;
    }
}