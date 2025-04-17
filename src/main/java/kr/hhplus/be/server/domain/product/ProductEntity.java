package kr.hhplus.be.server.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Table(name="product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
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
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void updateStock(Long quantity) {
        if (quantity > this.stock) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
        }
        // 재고 차감
        this.stock -= quantity;
    }
    public void plusStock(Long quantity) {
        if (quantity > this.stock) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
        }
        // 재고 차감
        this.stock += quantity;
    }

    public Long orderProductAmount(Long price, Long quantity) {
        return price * quantity;
    }
}
