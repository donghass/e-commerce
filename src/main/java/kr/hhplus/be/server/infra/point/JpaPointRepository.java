package kr.hhplus.be.server.infra.point;

import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPointRepository extends JpaRepository<PointEntity, Long> {
    Optional<PointEntity> findByUserId(Long userId);
}
