package kr.hhplus.be.server.domain.point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Table(name="pointHistory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity
public class PointHistoryEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
        private Long id;
        @Column(name = "pointId", nullable = false, unique = true)
        private Long pointId;
        @Column(nullable = false, name = "amount")
        private Long amount;
        @Column(nullable = false, name = "balance")
        private Long balance;
        @Column(nullable = false, name = "type")
        private Type type;
        @Column(nullable = false, name = "createdAt")
        @CreationTimestamp
        private LocalDateTime createdAt = LocalDateTime.now();
        @Column(nullable = false, name = "updatedAt")
        private LocalDateTime updatedAt = LocalDateTime.now();


    public enum Type {
        USE,      // 사용
        CHARGE      // 충전
    }
        // 포인트 사용, 충전 팩토리
        public static PointHistoryEntity save(Long pointId,Long amount, Long balance,Type type) {
                PointHistoryEntity pointHistory = new PointHistoryEntity();
                pointHistory.pointId = pointId;
                pointHistory.amount = amount;
                pointHistory.balance = balance;
                pointHistory.type = type;
                return pointHistory;
        }
}
