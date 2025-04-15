package kr.hhplus.be.server.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;


@Getter
@Entity
@Builder
@Table(name="order")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;

    private Long userId;

    @Column(nullable = false, name = "userCouponId")
    private Long userCouponId;

    @Column(nullable = false, name = "totalAmount")
    private Long totalAmount;

    @Builder.Default
    @Column(nullable = false, name = "status")
    private PaymentStatus status = PaymentStatus.NOT_PAID;

    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updatedAt")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드 패턴을 통해 빌더를 이용해 객체를 생성
    public static OrderEntity create(Long userId, Long totalAmount, Long userCouponId){
        return OrderEntity.builder()
                .userId(userId)
                .userCouponId(userCouponId)
                .totalAmount(totalAmount)
                .build();
    }

    public void updateOrderStatus(PaymentStatus status){
        this.status=status;
    }

    public enum PaymentStatus {
        NOT_PAID,      // 미결제
        PAID,      // 결제
        EXPIRED     // 주문 시간 만료
    }
}

