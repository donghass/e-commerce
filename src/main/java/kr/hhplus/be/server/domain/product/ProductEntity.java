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
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 이게 비즈니스로직을 도메인안에 넣는이라 하긴 좀 그런데요
    // 이전에는 service에서 ProductEntity의 stock을 쿼리로 바꿨잖아요? 그건 폭력적인 방법이에요
    // ProductEntity에게 너의 stock은 네가 바꾸는거라고 책임을 주는거에요
    public void updateStock(Long deductQuantity){
        if (deductQuantity > this.stock) {
            throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
        }
        // 재고 차감
        this.stock-= deductQuantity;
    }
}
