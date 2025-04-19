package kr.hhplus.be.server.infra.point;

import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPointHistoryRepository extends JpaRepository<PointHistoryEntity, Long> {
}
