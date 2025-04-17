package kr.hhplus.be.server;

import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
import kr.hhplus.be.server.cleanUp.DbCleaner;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.ProductEntity;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
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
        System.out.println("Saved coupon ID: " + dummyCoupon.getId());
        couponRepository.flush();            // ✨ 실제 DB에 반영
        entityManager.clear();               // ✨ 영속성 컨텍스트 비워주기

        CouponIssueRequest request = new CouponIssueRequest(100L,savedCoupon.getId());

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}