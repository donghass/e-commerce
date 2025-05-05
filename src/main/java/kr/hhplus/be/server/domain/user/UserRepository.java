package kr.hhplus.be.server.domain.user;


import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {

    Optional<UserEntity> findById(Long aLong);

    List<UserEntity> saveAll(List<UserEntity> dummyUser);

    UserEntity save(UserEntity user);
}
