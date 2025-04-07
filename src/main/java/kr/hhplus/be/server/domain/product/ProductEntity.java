package kr.hhplus.be.server.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

@Table(name="product")
@Data
@DynamicUpdate // 실제 변경한 컬럼만 업데이트
@Entity
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //pk의 sequential 값을 자동 증가
    private Long id;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(nullable = false, name = "price")
    private Long price;
    @Column(nullable = false, name = "stock")
    private Long stock;
    @Column(nullable = false, name = "createdAt")
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false, name = "updatedAt")
    private LocalDateTime updatedAt;
}
