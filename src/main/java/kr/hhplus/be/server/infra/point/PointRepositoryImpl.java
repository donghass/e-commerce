package kr.hhplus.be.server.infra.point;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final JpaPointRepository jpaPointRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때

    @Override
    public Optional<PointEntity> findByUserId(Long userId) {
        return jpaPointRepository.findByUserId(userId);
    }



}