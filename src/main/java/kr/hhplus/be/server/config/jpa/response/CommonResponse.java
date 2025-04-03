package kr.hhplus.be.server.config.jpa.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResponse<T> {
    private int code;
    private String status;
    private String message;
    private T data;
}