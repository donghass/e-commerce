package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;


@Table(name="coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
@ToString
public class CouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "discountValue")
    private Long discountValue;
    @Column(nullable = false, name = "discountType")
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    @Column(nullable = false, name = "startDate")
    private LocalDateTime startDate;
    @Column(nullable = false, name = "endDate")
    private LocalDateTime endDate;
    @Column(nullable = false, name = "stock")
    private Long stock;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void couponUpdate() {
        // 잔여 수량 없으면 실패
        if(this.stock <= 0){
            throw new BusinessException(CouponErrorCode.COUPON_OUT_OF_STOCK);
        }
        // 쿠폰 갯수 차감
        this.stock -= 1;
    }


    public enum DiscountType {
        RATE,       // 정률
        AMOUNT      // 정액
    }

    public static CouponEntity save(String name, Long discountValue, DiscountType discountType, Long stock) {
        CouponEntity coupon = new CouponEntity();
        coupon.name = name;
        coupon.discountValue = discountValue;
        coupon.discountType = discountType;
        coupon.stock = stock;
        return coupon;
    }

    public static Long discount(Long amount, CouponEntity coupon) {
        return DiscountPolicy.discount(amount, coupon);
    }
}

