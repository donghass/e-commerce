package kr.hhplus.be.server.api.coupon;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.coupon.UserCouponListResult;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponWithCouponDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor    // 생성자 자동 생성
public class CouponController implements CouponControllerDocs {
    private final CouponFacade couponFacade;

    @Operation(summary = "사용자 쿠폰 조회", description = "userId를 이용해 해당 사용자의 보유 쿠폰 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<UserCouponListResponse>> getCoupons(@RequestParam Long userId){
        UserCouponListResult result = couponFacade.getUserCoupons(userId);
        UserCouponListResponse response = UserCouponListResponse.from(result);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS, response));
    }
    @Operation(summary = "사용자 선착순 쿠폰 발급", description = "userId를 이용해 해당 사용자의 쿠폰을 발급합니다.")
    @PostMapping("/issue")
    public ResponseEntity<CommonResponse<Object>> issueCoupon(@Valid @RequestBody CouponIssueRequest request) {
        couponFacade.createCoupon(request.toCommand());

        // 성공적으로 처리되면 204 No Content 반환
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS));
    }
}
