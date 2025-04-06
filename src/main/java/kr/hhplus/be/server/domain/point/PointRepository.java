package kr.hhplus.be.server.domain.point;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository {
    Optional<PointEntity> findByUserId(Long userId);
}
