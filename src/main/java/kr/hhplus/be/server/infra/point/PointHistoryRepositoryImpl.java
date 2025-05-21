package kr.hhplus.be.server.infra.point;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.point.PointHistoryEntity;
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final JpaPointHistoryRepository jpaPointHistoryRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때


    @Override
    public void save(PointHistoryEntity pointHistory) {
        jpaPointHistoryRepository.save(pointHistory);
    }

    @Override
    public void delete(Long id) {
        jpaPointHistoryRepository.deleteById(id);
    }
}