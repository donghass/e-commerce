package kr.hhplus.be.server.infra.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품 리스트 조회용 dto
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryDto {
    private Long id;
    private String name;
    private Long price;
    private Long stock;
}
