package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.domain.point.PointEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


@Table(name="userCoupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
public class UserCouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @JoinColumn(nullable = false, name = "userId")
    private Long userId;
    @JoinColumn(nullable = false, name = "couponId")
    private Long couponId;
    @Column(nullable = false, name = "isUsed")
    private boolean isUsed = false;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = true, name = "issuedAt")
    private LocalDateTime issuedAt;  //사용일
    @Column(nullable = false, name = "expiredAt")
    private LocalDateTime expiredAt; //만료일  발급일 + 7일
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static UserCouponEntity save(Long userId, String name, Long couponId, LocalDateTime localDateTime) {
        UserCouponEntity userCoupon = new UserCouponEntity();
        userCoupon.userId = userId;
        userCoupon.name = name;
        userCoupon.couponId = couponId;
        userCoupon.expiredAt = localDateTime;
        return userCoupon;
    }


    public void validateCoupon(UserCouponEntity userCoupon) {
// 1. 쿠폰 유효성 검증
        if (userCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(CouponErrorCode.COUPON_EXPIRED);
        }
        // boolean 타입
        if (userCoupon.isUsed()) {
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_USED);
        }
    }
}


