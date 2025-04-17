package kr.hhplus.be.server.api.product;

import kr.hhplus.be.server.application.product.BestSellerResult;

public record BestSellerResponse(Long id, String name, Long price, Long stock, Long sales) {
    //  리스트로 받기
// 단일 DTO 변환 (페이징용)
    public static BestSellerResponse from(BestSellerResult dto) {
        return new BestSellerResponse(dto.productId(), dto.name(), dto.price(), dto.stock(), dto.sales());
    }
}
