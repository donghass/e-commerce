package kr.hhplus.be.server.domain.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.user.UserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


@Table(name="orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
@Builder
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
    @Builder.Default
    @Column(nullable = false, name = "totalAmount")
    private Long totalAmount = 0L;
    @Column(nullable = false, name = "status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.NOT_PAID;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProductEntity> orderItems = new ArrayList<>();

    public void updateStatus(PaymentStatus paymentStatus) {
        this.status = paymentStatus;
    }


    public enum PaymentStatus {
        NOT_PAID,      // 미결제
        PAID,      // 결제
        EXPIRED     // 주문 시간 만료
    }

    public static OrderEntity create(UserEntity user, UserCouponEntity userCoupon) {
        OrderEntity order = new OrderEntity();
        order.userId = user.getId();
        if (userCoupon != null) {
            order.userCouponId = userCoupon.getCouponId();
        }
        return order;
    }

    public void addOrderProduct(ProductEntity product, Long quantity) {
        Long amount = product.getPrice() * quantity;

        OrderProductEntity item = OrderProductEntity.create(
            product.getId(), this, amount, quantity
        );

        this.orderItems.add(item);
        this.totalAmount += amount;
    }
    public void discountApply(CouponEntity coupon) {
        Long discountPrice = coupon.discount(totalAmount, coupon);
        this.totalAmount -= discountPrice;
    }

}

