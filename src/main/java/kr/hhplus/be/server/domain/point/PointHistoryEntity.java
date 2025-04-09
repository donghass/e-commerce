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
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.point.execption.PointErrorCode;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

@Table(name="pointHistory")
@Data
@DynamicUpdate // 실제 변경한 컬럼만 업데이트
@Entity
public class PointHistoryEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
        private Long id;
        @OneToOne   // 1:1 관계에 외래키
        @JoinColumn(name = "pointId", nullable = false, unique = true)
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
}
