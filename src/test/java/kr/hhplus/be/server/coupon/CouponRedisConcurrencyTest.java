package kr.hhplus.be.server.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.awaitility.Awaitility;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("쿠폰 발급 레디스 분산락 동시성 테스트")
    void createCoupon_concurrency_test() throws InterruptedException {
        // Arrange

        List<UserEntity> dummyUser = IntStream.range(0, 100) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        List<CouponEntity> dummyCoupon = IntStream.range(0, 1) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(CouponEntity.class)
                .ignore(field(CouponEntity.class, "id"))
                .set(field(CouponEntity.class, "stock"), 100L)
                .create())
            .toList();

        List<CouponEntity> savedCoupon = couponRepository.saveAll(dummyCoupon);

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();

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

        // 종료 시간 기록
        long endTime = System.currentTimeMillis();
        // 실행 시간 계산 및 출력
        long duration = endTime - startTime;
        System.out.println("총 실행 시간(ms) = " + duration);

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

    @Test
    @DisplayName("100명 동시 쿠폰 발급 - 정확히 100명만 성공 카프카 메시지 발행")
    void issueCoupon_concurrent_100Users() throws Exception {
        // given
        int userCount = 110;

        List<UserEntity> dummyUser = IntStream.range(0, 110) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        List<CouponEntity> dummyCoupon = IntStream.range(0, 1) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(CouponEntity.class)
                .ignore(field(CouponEntity.class, "id"))
                .set(field(CouponEntity.class, "stock"), 100L)
                .create())
            .toList();

        List<CouponEntity> savedCoupon = couponRepository.saveAll(dummyCoupon);

        String stockKey = "coupon:stock:" + savedCoupon.get(0).getId();
        String issuedKey = "coupon:issued:" + savedCoupon.get(0).getId();
        redisTemplate.delete(stockKey);
        redisTemplate.delete(issuedKey);

        // Redis에 재고 100개 세팅
        for (int i = 0; i < 100; i++) {
            redisTemplate.opsForList().rightPush(stockKey, UUID.randomUUID().toString());
        }

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(userCount);

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // when: 110명의 유저가 동시에 발급 시도
        for (int i = 0; i < userCount; i++) {
            final Long userId = (long) i + 1;
            executor.submit(() -> {
//                eventPublisher.publishEvent(new CouponIssueEvent(userId, savedCoupon.get(0).getId()));
            couponFacade.issueCouponAsync(new CouponIssueCommand(userId, savedCoupon.get(0).getId()));
                latch.countDown();
            });
        }

        latch.await(); // 모든 요청 완료 대기

        // then: 비동기 처리 기다리기
//        Awaitility.await().atMost(Duration.ofSeconds(100)).until(() ->
//            userCouponRepository.count(savedCoupon.get(0).getId()) == 100
//        );

        // 종료 시간 기록
        long endTime = System.currentTimeMillis();
        // 실행 시간 계산 및 출력
        long duration = endTime - startTime;
        System.out.println("총 실행 시간(ms) = " + duration);
        Thread.sleep(100000);
        long successCount = userCouponRepository.count(savedCoupon.get(0).getId());
        long redisStockLeft = redisTemplate.opsForList().size(stockKey);
        long redisSetSize = redisTemplate.opsForSet().size(issuedKey);

        assertThat(successCount).isEqualTo(100);      // DB에 100명만 발급됨
        assertThat(redisStockLeft).isEqualTo(0);      // 재고는 0
        assertThat(redisSetSize).isEqualTo(100);      // Redis Set에도 100명만 있음
    }
}