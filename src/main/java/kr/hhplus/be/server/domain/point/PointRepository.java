package kr.hhplus.be.server.domain.point;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository {

    default void usePoint(Long userId, Long balance) {
    }

    Optional<PointEntity> findByUserId(Long userId);

    void charge(Long userId, Long amount);

    PointEntity saveAndFlush(PointEntity dummyPoint);
}
