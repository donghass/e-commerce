package kr.hhplus.be.server.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.cleanUp.DbCleaner;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class CouponRedisConcurrencyTest extends IntegerationTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserCouponRepository userCouponRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CouponFacade couponFacade;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("쿠폰 발급 레디스 분산락 동시성 테스트")
    void createCoupon_concurrency_test() throws InterruptedException {
        // Arrange

        List<UserEntity> dummyUser = IntStream.range(0, 50) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        List<CouponEntity> dummyCoupon = IntStream.range(0, 1) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(CouponEntity.class)
                .ignore(field(CouponEntity.class, "id"))
                .set(field(CouponEntity.class, "stock"), 1L)
                .create())
            .toList();

        List<CouponEntity> savedCoupon = couponRepository.saveAll(dummyCoupon);

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    CouponIssueCommand command = new CouponIssueCommand(Long.valueOf(finalI), 1L); // couponId 1번, userId i번
                    couponFacade.createCoupon(command);
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        latch.await();
        executorService.shutdown();
//      성공 갯수 카운트
        long successCount = futures.stream()
            .filter(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    return false;
                }
            })
            .count();

        // Assert
        assertThat(successCount).isEqualTo(1L);
        System.out.println("쿠폰발급 성공 횟수 → " + successCount);
    }
}