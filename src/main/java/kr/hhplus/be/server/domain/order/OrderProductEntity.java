package kr.hhplus.be.server.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Table(name="order_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
@Builder
public class OrderProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @Column(nullable = false, name = "productId")
    private Long productId;
//    @Column(nullable = false, name = "ordersId")
//    private Long orderId;
    @Column(nullable = false, name = "amount")
    private Long amount;
    @Column(nullable = false, name = "quantity")
    private Long quantity;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))  //FK 생성 X
    private OrderEntity order;

    public static OrderProductEntity create(Long productId, OrderEntity order, Long amount, Long quantity) {
        OrderProductEntity orderItem = new OrderProductEntity();
        orderItem.productId = productId;
        orderItem.order = order;
        orderItem.amount = amount;
        orderItem.quantity = quantity;
        return orderItem;
    }
}