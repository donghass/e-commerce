package kr.hhplus.be.server.coupon;

import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class CouponControllerIntegrationTest extends IntegerationTestSupport {

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
    private EntityManager entityManager;

    @Autowired
    private DbCleaner dbCleaner;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_success() throws Exception {
        // given
        CouponEntity dummyCoupon = Instancio.of(CouponEntity.class)
            .ignore(Select.field(CouponEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(CouponEntity.class, "discountValue"), 1000L)
            .set(Select.field(CouponEntity.class, "discountType"), DiscountType.RATE)
            .set(Select.field(CouponEntity.class, "startDate"), LocalDateTime.now().minusDays(1))
            .set(Select.field(CouponEntity.class, "endDate"), LocalDateTime.now().plusDays(7))
            .set(Select.field(CouponEntity.class, "stock"), 100L)
            .create();

        CouponEntity savedCoupon = couponRepository.saveAndFlush(dummyCoupon);
        System.out.println("=== Dummy Coupons ===");
        System.out.println("Saved coupon ID: " + savedCoupon.getId());

        CouponIssueRequest request = new CouponIssueRequest(100L,savedCoupon.getId());

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("userId를 이용해 보유 쿠폰 목록을 정상 조회한다")
    void getUserCoupons_success() throws Exception {
        // given
        Long userId = 1L;


        List<CouponEntity> dummyCoupon = IntStream.range(0, 100) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(CouponEntity.class)
                .ignore(Select.field(CouponEntity.class, "id"))
                .create())
            .toList();

        couponRepository.saveAll(dummyCoupon);

        List<UserCouponEntity> userCouponDummyList = IntStream.range(0, 100) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserCouponEntity.class)
                .ignore(Select.field(UserCouponEntity.class, "id"))
                .create())
            .toList();

        userCouponRepository.saveAll(userCouponDummyList);

        // when & then
        mockMvc.perform(get("/api/v1/coupons")
                .param("userId", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200)) // code 필드 확인
            .andExpect(jsonPath("$.data.userId").value(userId)) // 응답 데이터 확인
            .andExpect(jsonPath("$.data.coupons").isArray()); // coupon 목록이 배열인지 확인
    }

    @Test
    @DisplayName("쿠폰 발급 비관적락 동시성 테스트")
    void issueCoupon_concurrent_withStockLimit() throws Exception {
        dbCleaner.execute();

        List<UserEntity> dummyUser = IntStream.range(0, 10) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(Select.field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        List<CouponEntity> dummyCoupon = IntStream.range(0, 1) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(CouponEntity.class)
                .ignore(Select.field(CouponEntity.class, "id"))
                .set(Select.field(CouponEntity.class, "stock"), 10L)
                .create())
            .toList();

        List<CouponEntity> savedCoupon = couponRepository.saveAll(dummyCoupon);
        System.out.println("시작 쿠폰 = " +savedCoupon.get(0).getStock());

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    CouponIssueRequest request = new CouponIssueRequest(savedUser.get(finalI).getId(),1L);

                    mockMvc.perform(post("/api/v1/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Optional<CouponEntity> couponCount = couponRepository.findById(1L);
        Assertions.assertEquals(0, couponCount.get().getStock(), "쿠폰 수량 일치하지 않습니다.");
    }
}