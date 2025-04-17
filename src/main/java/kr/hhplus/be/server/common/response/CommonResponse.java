package kr.hhplus.be.server.common.response;

import java.sql.Timestamp;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder                // 빌더 패턴
public class CommonResponse<T> {
    private int code;
    private String status;
    private String message;
    private Timestamp createdAt;
    private T data;


    // 반환값 있는 성공 응답
    public static <T> CommonResponse<T> success(BaseResponseCode code, T data) {
        return CommonResponse.<T>builder()
            .code(code.getCode())
            .status(code.getStatus())
            .message(code.getMessage())
            .data(data)
            .createdAt(Timestamp.from(Instant.now())) // 발생 시간!
            .build();
    }
    // 반환값 없는 성공 응답
    public static <T> CommonResponse<T> success(BaseResponseCode code) {
        return CommonResponse.<T>builder()
            .code(code.getCode())
            .status(code.getStatus())
            .message(code.getMessage())
            .createdAt(Timestamp.from(Instant.now()))
            .build();
    }

    // 실패 응답
    public static <T> CommonResponse<T> fail(BaseResponseCode code) {
        return CommonResponse.<T>builder()
            .code(code.getCode())
            .status(code.getStatus())
            .message(code.getMessage())
            .createdAt(Timestamp.from(Instant.now()))
            .build();
    }
}

