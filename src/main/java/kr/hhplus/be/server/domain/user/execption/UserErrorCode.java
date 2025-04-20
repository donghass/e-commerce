package kr.hhplus.be.server.domain.user.execption;

import kr.hhplus.be.server.common.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseResponseCode {

    // 400 - 잘못된 요청
    INVALID_USER_ID(400, "사용자 ID 유효하지 않음", "유효하지 않은 사용자 ID입니다."),
    INVALID_ORDER_ID(400, "주문 ID 유효하지 않음", "유효하지 않은 주문 ID입니다."),
    INVALID_QUANTITY(400, "수량 요청 유효하지 않음", "수량은 1 이상이어야 합니다."),

    // 404 - 리소스 없음
    USER_NOT_FOUND(404, "사용자 존재하지 않음", "사용자를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(404, "주문 존재하지 않음", "주문을 찾을 수 없습니다."),

    // 409 - 비즈니스 충돌
    ORDER_STATUS_INVALID(409, "주문 상태가 결제 불가능", "결제가 불가능한 주문 상태입니다.");


    private final int code;         // HTTP 상태 코드
    private final String status;    // 커스텀 코드
    private final String message;   // 사용자 메시지
}