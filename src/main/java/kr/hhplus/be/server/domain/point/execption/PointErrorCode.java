package kr.hhplus.be.server.domain.point.execption;

import kr.hhplus.be.server.common.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements BaseResponseCode {

    // 🔹 400 - 잘못된 요청
    INVALID_USER_ID(400, "사용자 ID 유효하지 않음", "유효하지 않은 사용자 ID입니다."),
    INVALID_CHARGE_AMOUNT(400, "충전 금액이 0 이하", "충전 금액은 0 보다 커야 합니다."),

    // 🔹 409 - 비즈니스 충돌
    EXCEED_ONE_TIME_LIMIT(409, "1회 충전 금액 초과", "1회 최대 충전 금액을 초과했습니다."),
    EXCEED_TOTAL_CHARGE_LIMIT(409, "누적 금액 초과", "누적 충전 가능 금액을 초과했습니다."),
    POINT_BALANCE_INSUFFICIENT(409, "포인트 부족", "포인트가 부족합니다."),
    CONFLICT(409, "충전 실패", "충전 중 충돌이 발생했습니다. 다시 시도해주세요."),

    // 🔹 404 - 리소스 없음
    USER_NOT_FOUND(404, "사용자 존재하지 않음", "사용자를 찾을 수 없습니다.");


    private final int code;         // HTTP 상태 코드
    private final String status;    // 커스텀 코드
    private final String message;   // 사용자 메시지
}