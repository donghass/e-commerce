package kr.hhplus.be.server.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "best_seller")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BestSellerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "productId")
    private Long productId;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "price")
    private Long price;
    @Column(nullable = false, name = "stock")
    private Long stock;
    @Column(nullable = false, name = "sales")
    private Long sales;

    @Column(nullable = false, name = "createdAt")
    private LocalDateTime createdAt  = LocalDateTime.now();
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();


}