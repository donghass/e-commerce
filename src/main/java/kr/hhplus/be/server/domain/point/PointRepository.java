package kr.hhplus.be.server.domain.point;

import java.util.Optional;

public interface PointRepository {

    default void usePoint(Long userId, Long balance) {
    }

    Optional<PointEntity> findByUserId(Long userId);
}
