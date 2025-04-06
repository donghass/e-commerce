package kr.hhplus.be.server.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<PointEntity> findByUserId(Long userId);
}
