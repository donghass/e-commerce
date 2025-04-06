package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    public PointDto getPoint(Long userId) {
        PointEntity point = pointRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저의 포인트 정보가 없습니다."));

        return new PointDto(point.getUserId(), point.getBalance());
    }
}
