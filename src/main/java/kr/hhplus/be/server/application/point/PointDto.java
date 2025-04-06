package kr.hhplus.be.server.application.point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor    // 생성자 자동 생성
public class PointDto {
    private Long userId;
    private Long balance;

}
