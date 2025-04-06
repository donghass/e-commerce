package kr.hhplus.be.server.api.order;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import lombok.Data;
import lombok.Getter;

@Getter
public class OrderRequest {

    @NotNull(message = "userId는 필수입니다.")
    @Positive(message = "userId는 양의 정수여야 합니다.")
    private Long userId;

    @Positive(message = "userCouponId는 양의 정수여야 합니다.")
    private Long userCouponId; // 선택 항목 (nullable)

    @NotEmpty(message = "orderItems는 최소 1개 이상이어야 합니다.")
    private ArrayList<OrderItem> orderItems; // ArrayList로 변경

    @Data
    public static class OrderItem {

        @NotNull(message = "productId는 필수입니다.")
        @Positive(message = "productId는 양의 정수여야 합니다.")
        private Long productId;

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "quantity는 최소 1 이상이어야 합니다.")
        private Integer quantity;
    }
}