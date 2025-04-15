package kr.hhplus.be.server.domain.point;

import java.util.Optional;
import kr.hhplus.be.server.domain.point.PointHistoryEntity.Type;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository {


    void save(PointHistoryEntity pointHistory);

}
