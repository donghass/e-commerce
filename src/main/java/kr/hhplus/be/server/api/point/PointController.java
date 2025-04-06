package kr.hhplus.be.server.api.point;

import kr.hhplus.be.server.api.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
public class PointController implements PointControllerDocs {
//    @Operation(summary = "사용자 포인트 충전", description = "사용자 포인트를 충전합니다.")
    @PostMapping("/charge")
    public ResponseEntity<CommonResponse<PointResponse>> charge(@RequestBody ChargePointRequest request) {


        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
//    @Operation(summary = "사용자 포인트 조회", description = "사용자 포인트를 조회합니다.")
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
//  결제
    @PostMapping("/use")
    public ResponseEntity<?> usePoints(@RequestBody UsePointsRequest request) {
        // 1. 주문 ID에 해당하는 주문 상태를 조회
        boolean isOrderValid = checkOrderStatus(request.getOrderId());

        // 2. 포인트 잔액 조회 (예시로 100,000원이라고 가정)
        long availablePoints = 100000;  // 포인트 잔액 100,000원
        long paymentAmount = request.getOrderId();  // 결제 금액 (orderId와 실제 결제 금액이 매핑되는 구조일 것)

        // 5. 성공적으로 처리되면 204 No Content 반환
        return ResponseEntity.noContent().build();
    }

    // 주문 상태 확인 (예시)
    private boolean checkOrderStatus(Long orderId) {
        // 실제로는 DB에서 주문 상태를 조회해야 합니다.
        // 예시로 orderId가 1이면 EXPIRED 상태로 처리
        if (orderId == 1) {
            return false; // EXPIRED 상태로 처리
        }

        return true; // 그 외의 상태는 정상
    }

}
