package kr.hhplus.be.server.config.jpa.response;

import lombok.Getter;

// 포인트 사용 API (결제) 응답 DTO
@Getter
public class ApiResponse {
    private int code;
    private String message;
    private String detail;


}
