package kr.hhplus.be.server.api.point;

import kr.hhplus.be.server.application.point.PointDto;

public record PointResponse(Long userId, Long balance) {

        public static PointResponse from(PointDto dto) {
                return new PointResponse(dto.userId(), dto.balance());
        }

}
