package kr.hhplus.be.server.infra.point;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.QPointEntity;
import kr.hhplus.be.server.domain.product.QProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final JpaPointRepository jpaPointRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;

    QPointEntity point = QPointEntity.pointEntity;

    @Override
    public void usePoint(Long userId, Long balance) {
        queryFactory.update(point).set(point.balance,balance).where(point.userId.eq(userId)).execute();
    }

    @Override
    public Optional<PointEntity> findByUserId(Long userId) {
        return jpaPointRepository.findByUserId(userId);
    }

    @Override
    public void charge(Long userId, Long amount) {
        queryFactory.update(point).set(point.balance,amount).where(point.userId.eq(userId)).execute();
    }

    @Override
    public PointEntity saveAndFlush(PointEntity dummyPoint) {
        return jpaPointRepository.saveAndFlush(dummyPoint);
    }

}