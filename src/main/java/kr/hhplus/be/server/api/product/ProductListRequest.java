package kr.hhplus.be.server.api.product;

import lombok.Getter;

// 상품 목록 조회 요청 DTO
@Getter
public class ProductListRequest {
    // 필요한 요청 파라미터를 여기에 추가하세요 (예: 카테고리, 가격 범위 등)
    // 예시로 카테고리 필터를 추가할 수 있습니다.
    private String category;


}
