package kr.hhplus.be.server.coupon;

import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.user.UserEntity;
import org.instancio.Instancio;
import org.instancio.Select;
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
    private EntityManager entityManager;

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
}