package kr.hhplus.be.server.common.response;

public interface BaseResponseCode {
    int getCode();       // HTTP 상태 코드 (200, 400, 500 등)
    String getStatus();   // 커스텀 에러/성공 코드
    String getMessage();   // 사용자에게 보여줄 메시지
}