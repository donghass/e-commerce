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
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;


@Table(name="order")
@Data
@DynamicUpdate // 실제 변경한 컬럼만 업데이트
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @OneToOne   // 1:1 관계에 외래키
    @JoinColumn(nullable = false, name = "userId", unique = true)
    private Long userId;
    @Column(nullable = false, name = "userCouponId")
    private Long userCouponId;
    @Column(nullable = false, name = "isCouponApplied")
    private Long isCouponApplied;
    @Column(nullable = false, name = "totalAmount")
    private Long totalAmount;
    @Column(nullable = false, name = "status")
    private PaymentStatus status;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt;


    public enum PaymentStatus {
        PAID,       // 결제 완료
        NOT_PAID      // 미결제
    }
}

