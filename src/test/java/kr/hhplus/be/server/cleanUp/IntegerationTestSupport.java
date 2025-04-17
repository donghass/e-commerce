package kr.hhplus.be.server.cleanUp;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class IntegerationTestSupport {

    @Autowired
    private DbCleaner dbCleaner;

    @BeforeEach
    public void setUp() {
        dbCleaner.execute();
    }

}
