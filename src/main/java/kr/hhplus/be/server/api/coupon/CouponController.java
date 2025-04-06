package kr.hhplus.be.server.api.coupon;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import kr.hhplus.be.server.api.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponControllerDocs {
//    @Operation(summary = "사용자 쿠폰 조회", description = "userId를 이용해 해당 사용자의 보유 쿠폰 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CouponResponse>>> getCoupons(
        @RequestParam @NotNull(message = "userId는 필수입니다.")
        @Min(value = 1, message = "userId는 양의 정수여야 합니다.")
        Long userId
    ) {
        // 실제 DB 조회 대신 Mock 데이터
        LocalDate localDate = LocalDate.parse("2025-08-01");
        Date startDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate localDate2 = LocalDate.parse("2025-09-01");
        Date endDate = Date.from(localDate2.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<CouponResponse> coupons = Arrays.asList(
            new CouponResponse(1L, "할인쿠폰 1만원", "AMOUNT", "10000",startDate,endDate,"used"),
            new CouponResponse(2L, "10% 할인쿠폰", "RATE", "10",startDate,endDate,"notUsed")
        );

        CommonResponse<List<CouponResponse>> response = new CommonResponse<>(
            200,
            "OK",
            "사용자 보유 쿠폰이 정상적으로 조회되었습니다.",
            coupons
        );

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "사용자 쿠폰 발급", description = "userId를 이용해 해당 사용자의 쿠폰을 발급합니다.")
    @PostMapping("/issue")
    public ResponseEntity<CommonResponse<Object>> issueCoupon(@Valid @RequestBody CouponIssueRequest request) {
        // 실제 발급 로직은 생략 (여기서는 선착순 여부 확인 등 생략)

        CommonResponse<Object> response = new CommonResponse<>(
            201,
            "Created",
            "요청이 정상적으로 처리되었습니다.",
            Collections.emptyMap() // 빈 객체 {}
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
