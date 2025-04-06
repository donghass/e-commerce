package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.response.BaseResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BaseResponseCode baseResponseCode ;

    public BusinessException(BaseResponseCode baseResponseCode) {
        super(baseResponseCode.getMessage());
        this.baseResponseCode = baseResponseCode;
    }
}
