package kr.hhplus.be.server.controller.mock;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.hhplus.be.server.config.jpa.request.ChargeRequest;
import kr.hhplus.be.server.config.jpa.response.CommonResponse;
import kr.hhplus.be.server.config.jpa.response.CouponResponse;
import kr.hhplus.be.server.config.jpa.response.PointResponse;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {
    @Operation(summary = "사용자 포인트 충전", description = "사용자 포인트를 충전합니다.")
    @PostMapping("/charge")
    public ResponseEntity<CommonResponse<PointResponse>> charge(@RequestBody ChargeRequest request) {


        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @Operation(summary = "사용자 포인트 조회", description = "사용자 포인트를 조회합니다.")
    @GetMapping("/userId={userId}")
    public ResponseEntity<PointResponse> getPoint(@PathVariable Long userId) {
        // Mock 데이터 생성
        PointResponse dto = new PointResponse();
        CommonResponse<PointResponse> response = new CommonResponse<>(
            200,
            "OK",
            "요청이 정상적으로 처리되었습니다.",
            dto
        );

        return ResponseEntity.ok(response.getData());
    }
}
