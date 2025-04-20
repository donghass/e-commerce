package kr.hhplus.be.server.domain.point;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository {

    Optional<PointEntity> findByUserId(Long userId);

    PointEntity saveAndFlush(PointEntity dummyPoint);

    void save(PointEntity point);
}
