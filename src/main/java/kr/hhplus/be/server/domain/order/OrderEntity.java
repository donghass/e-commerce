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
    //@Column(nullable = false, name = "isCouponApplied")   userCoupon에서 쿠폰사용여부 값 있고 order에 쿠폰 사용일 경우에만 userCouponId 들어오기 떄문에 필요 없다
    //private Long isCouponApplied;
    @Column(nullable = false, name = "totalAmount")
    private Long totalAmount;
    @Column(nullable = false, name = "status")
    private PaymentStatus status = PaymentStatus.NOT_PAID;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();


    public enum PaymentStatus {
        NOT_PAID,      // 미결제
        PAID      // 결제
    }
}

