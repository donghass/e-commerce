package kr.hhplus.be.server.api.product;

import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.product.ProductDto;

public record ProductListResponse(Long id, String name, Long price, Long stock) {
//  리스트로 받기
    public static List<ProductListResponse> from(List<ProductDto> dtos) {
        return dtos.stream()
            .map(dto -> new ProductListResponse(dto.id(), dto.name(), dto.price(), dto.stock()))
            .collect(Collectors.toList());
    }
}
