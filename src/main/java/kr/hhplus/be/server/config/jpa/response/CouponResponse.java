package kr.hhplus.be.server.config.jpa.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CouponResponse {
    private Long couponId;
    private String couponName;
    private String type;
    private String discountAmount; // 또는 int, Integer → 타입 정확히 맞추기
    private Date startDate;
    private Date endDate;
    private String status;
}