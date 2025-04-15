package kr.hhplus.be.server.api.product;

import kr.hhplus.be.server.application.product.ProductResult;

public record ProductListResponse(Long id, String name, Long price, Long stock) {
//  리스트로 받기
// 단일 DTO 변환 (페이징용)
    public static ProductListResponse from(ProductResult dto) {
        return new ProductListResponse(dto.id(), dto.name(), dto.price(), dto.stock());
    }
}
