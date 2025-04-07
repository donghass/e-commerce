package kr.hhplus.be.server.api.bestSeller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 인기 상품 조회 응답 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestSellerResponse {
    private int id;
    private String name;
    private long price;
    private int sales;
    private int stock;
}
