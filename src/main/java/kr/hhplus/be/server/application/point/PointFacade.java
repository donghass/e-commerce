package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.application.point.execption.PointErrorCode;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor    // 생성자 자동 생성
public class PointFacade {
    private final PointService pointService;

    public PointDto getPoint(Long userId) {
        if (userId <= 0) {
            throw new BusinessException(PointErrorCode.INVALID_USER_ID);  // 400
        }
        return pointService.getPoint(userId);
    }
}
