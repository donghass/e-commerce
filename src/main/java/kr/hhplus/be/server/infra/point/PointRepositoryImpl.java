package kr.hhplus.be.server.infra.point;

import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final JpaPointRepository jpaPointRepository;

    @Override
    public Optional<PointEntity> findByUserId(Long userId) {
        return jpaPointRepository.findByUserId(userId);
    }
}