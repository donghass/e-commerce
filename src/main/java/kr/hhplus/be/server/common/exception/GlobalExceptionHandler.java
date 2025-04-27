package kr.hhplus.be.server.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import kr.hhplus.be.server.common.response.BaseResponseCode;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        BaseResponseCode code = e.getBaseResponseCode();

        log.warn("[BusinessException] {} - {}", code.getStatus(), code.getMessage());

        return ResponseEntity
            .status(code.getCode())
            .body(CommonResponse.fail(code));
    }

    /**
     * 예상하지 못한 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("[Exception] 처리되지 않은 서버 오류 발생", e);

        return ResponseEntity
            .status(ResponseCode.INTERNAL_ERROR.getCode())
            .body(CommonResponse.fail(ResponseCode.INTERNAL_ERROR));
    }
}