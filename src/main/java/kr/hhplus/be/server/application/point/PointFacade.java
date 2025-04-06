package kr.hhplus.be.server.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointFacade {
    private final PointService pointService;

    public PointDto getPoint(Long userId) {
        return pointService.getPoint(userId);
    }
}
