package kr.hhplus.be.server.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


@Table(name="orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @Column(nullable = false, name = "userId")
    private Long userId;
    @Column(nullable = true, name = "userCouponId")
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
        PAID,      // 결제
        EXPIRED     // 주문 시간 만료
    }

    public static OrderEntity create(Long userId, Long userCouponId, Long totalAmount) {
        OrderEntity order = new OrderEntity();
        order.userId = userId;
        order.userCouponId = userCouponId;
        order.totalAmount = totalAmount;
        return order;
    }
}

