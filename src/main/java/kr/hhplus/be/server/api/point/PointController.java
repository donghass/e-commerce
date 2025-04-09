package kr.hhplus.be.server.api.point;

import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.application.point.PointDto;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointController implements PointControllerDocs {

    @Autowired
    private final PointFacade pointFacade;

//    @Operation(summary = "사용자 포인트 충전", description = "사용자 포인트를 충전합니다.")
    @PostMapping("/charge")
    public ResponseEntity<CommonResponse<PointResponse>> charge(@RequestBody ChargePointRequest request) {
//        ChargePointCommand command = request.toCommand();
//        PointDto pointDto = pointFacade.chargePoint(command);
//
//        // DTO → Response 객체로 변환 // 반환 값이 많지 않으므로 record 사용
//        PointResponse point = PointResponse.builder()
//            .userId(pointDto.userId())
//            .balance(pointDto.balance())
//            .build();
        PointDto dto = pointFacade.chargePoint(request.toCommand());
        CommonResponse<PointResponse> response = CommonResponse.success(ResponseCode.SUCCESS, PointResponse.from(dto));

        return ResponseEntity.ok(response);
    }
//    @Operation(summary = "사용자 포인트 조회", description = "사용자 포인트를 조회합니다.")
    @GetMapping("/userId={userId}")
    public ResponseEntity<CommonResponse<PointResponse>> getPoint(@PathVariable Long userId) {

        PointDto pointDto = pointFacade.readPoint(userId);

        // DTO → Response 객체로 변환  // 반환 값이 많지 않으므로 record 사용
//        PointResponse point = PointResponse.builder()
//            .userId(pointDto.userId())
//            .balance(pointDto.balance())
//            .build();
//
//        CommonResponse<PointResponse> response = CommonResponse.success(ResponseCode.SUCCESS, point);
        CommonResponse<PointResponse> response = CommonResponse.success(ResponseCode.SUCCESS, PointResponse.from(pointDto));

        return ResponseEntity.ok(response);
    }
//  결제
    @PostMapping("/use")
    public ResponseEntity<CommonResponse<PointResponse>> usePoint(@RequestBody UsePointsRequest request) {
        pointFacade.usePoint(request.getOrderId());

        // 성공적으로 처리되면 204 No Content 반환
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.SUCCESS));
    }



}
