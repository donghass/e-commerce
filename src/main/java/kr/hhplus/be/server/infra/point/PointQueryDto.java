package kr.hhplus.be.server.infra.point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 유저 포인트 조회용 dto
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointQueryDto {
    private Long userId;
    private Long balance;
}
