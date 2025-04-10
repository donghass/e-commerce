package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode implements BaseResponseCode{

    // 성공 코드
    SUCCESS(200, "SUCCESS", "요청이 정상적으로 처리되었습니다."),
    CREATED(201, "CREATED", "리소스가 성공적으로 생성되었습니다."),
    // 예상치 못 한 공통 오류
    INTERNAL_ERROR(500, "FAIL", "서버 오류가 발생했습니다.");

    private final int code;
    private final String status;
    private final String message;

}