package kr.hhplus.be.server.infra.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final JPAQueryFactory queryFactory; //복잡한 조건, 일부 필드만 조회, 최적화가 필요할 때
    private final EntityManager em;


    @Override
    public Optional<UserEntity> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public List<UserEntity> saveAll(List<UserEntity> dummyUser) {
        return jpaUserRepository.saveAll(dummyUser);
    }
}