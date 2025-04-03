package kr.hhplus.be.server.config.jpa.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 인기 상품 조회 응답 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProductResponse {
    private int id;
    private String name;
    private long price;
    private int sales;
    private int stock;
}
