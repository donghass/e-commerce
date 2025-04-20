package kr.hhplus.be.server.domain.point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


@Table(name="point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
public class PointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private Long userId;
    @Column(nullable = false, name = "balance")
    private Long balance;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version;  // 낙관적 락 관리용 버전

    public Long charge(Long amount) {
        if(this.balance + amount > 5000000){throw new BusinessException(PointErrorCode.EXCEED_TOTAL_CHARGE_LIMIT);}
        this.balance += amount;
        return this.balance;
    }
// 포인트 사용, 충전 팩토리
    public static PointEntity save(Long userId, Long balance) {
        PointEntity point = new PointEntity();
        point.userId = userId;
        point.balance = balance;
        return point;
    }

    public Long use(Long totalAmount) {
        if(balance <= 0){throw new BusinessException(PointErrorCode.POINT_BALANCE_INSUFFICIENT);}
        this.balance -= totalAmount;
        return this.balance;
    }
}
