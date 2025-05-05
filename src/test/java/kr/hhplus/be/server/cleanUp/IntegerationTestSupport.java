package kr.hhplus.be.server.cleanUp;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class IntegerationTestSupport {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DbCleaner dbCleaner;

    @BeforeEach
    public void setUp() {
        dbCleaner.execute();
        // 레디스에 저장된 모든 키 삭제
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

}
