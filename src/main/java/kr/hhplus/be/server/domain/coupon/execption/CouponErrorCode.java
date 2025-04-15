package kr.hhplus.be.server.domain.coupon.execption;

import kr.hhplus.be.server.common.response.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements BaseResponseCode {

    // 400 - 잘못된 요청
    INVALID_USER_ID(400, "사용자 ID 유효하지 않음", "유효하지 않은 사용자 ID입니다."),
    INVALID_COUPON_ID(400, "쿠폰 ID 유효하지 않음", "유효하지 않은 쿠폰 ID입니다."),

    // 404 - 리소스 없음
    USER_NOT_FOUND(404, "사용자 존재하지 않음", "사용자를 찾을 수 없습니다."),
    COUPON_NOT_FOUND(404, "쿠폰 존재하지 않음", "쿠폰을 찾을 수 없습니다."),

    // 409 - 비즈니스 충돌
    COUPON_NOT_OWNED(409, "쿠폰 보유하지 않음", "해당 쿠폰을 보유하고 있지 않습니다."),
    COUPON_EXPIRED(409, "쿠폰 유효기간 초과", "쿠폰 유효기간이 지났거나 아직 유효하지 않습니다."),
    COUPON_OUT_OF_STOCK(409, "잔여 수량 0", "쿠폰이 모두 소진되었습니다."),
    COUPON_ALREADY_ISSUED(409, "이미 발급받음", "이미 발급받은 쿠폰입니다."),
    COUPON_ALREADY_USED(409, "쿠폰 이미 사용됨", "이미 사용된 쿠폰입니다.");


    private final int code;         // HTTP 상태 코드
    private final String status;    // 커스텀 코드
    private final String message;   // 사용자 메시지
}