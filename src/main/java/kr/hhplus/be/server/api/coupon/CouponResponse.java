package kr.hhplus.be.server.api.coupon;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

public record CouponResponse(
    Long couponId,
    String couponName,
    String type,
    Long discountAmount, // 타입을 int로 하고 싶으면 여기서 변경 가능
    LocalDateTime startDate,
    LocalDateTime endDate,
    String status
) {}