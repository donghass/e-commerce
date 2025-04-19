package kr.hhplus.be.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.order.OrderRequest;
import kr.hhplus.be.server.api.order.OrderRequest.OrderItem;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.UserEntity;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class OrderControllerIntegrationTest extends IntegerationTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserCouponRepository userCouponRepository;
    @Autowired
    private PointRepository pointRepository;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() throws Exception {

        // 상품 생성
        List<ProductEntity> dummyProducts = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(ProductEntity.class)
                .ignore(Select.field(ProductEntity.class, "id"))
                .set(Select.field(ProductEntity.class, "price"), 1000L + i * 500)  // 예: 1000, 1500, ...
                .set(Select.field(ProductEntity.class, "stock"), 10L)
                .create())
            .toList();

        List<ProductEntity> savedProduct = productRepository.saveAll(dummyProducts);
        // 쿠폰 생성 및 발급
        CouponEntity dummyCoupon = Instancio.of(CouponEntity.class)
            .ignore(Select.field(CouponEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(CouponEntity.class, "discountValue"), 1000L)
            .set(Select.field(CouponEntity.class, "discountType"), DiscountType.AMOUNT)
            .set(Select.field(CouponEntity.class, "stock"), 1L)
            .create();

        CouponEntity savedCoupon = couponRepository.saveAndFlush(dummyCoupon);

        UserCouponEntity dummyUserCoupon = Instancio.of(UserCouponEntity.class)
            .ignore(Select.field(UserCouponEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(UserCouponEntity.class, "couponId"), 1L)
            .set(Select.field(UserCouponEntity.class, "expiredAt"), LocalDateTime.now().plusDays(7))
            .set(Select.field(UserCouponEntity.class, "isUsed"), false)
            .create();

        UserCouponEntity savedUserCoupon = userCouponRepository.saveAndFlush(dummyUserCoupon);

        // 요청 객체 생성
        OrderRequest orderRequest = new OrderRequest();

// 필드 강제 세팅 (setter 없음 → Reflection 사용)
        ReflectionTestUtils.setField(orderRequest, "userId", 1L);
        ReflectionTestUtils.setField(orderRequest, "userCouponId", 1L);

// 주문 상품 아이템 생성
        OrderItem item1 = new OrderItem();
        ReflectionTestUtils.setField(item1, "productId", dummyProducts.get(0).getId());
        ReflectionTestUtils.setField(item1, "quantity", 3L);

        OrderItem item2 = new OrderItem();
        ReflectionTestUtils.setField(item2, "productId", dummyProducts.get(1).getId());
        ReflectionTestUtils.setField(item2, "quantity", 1L);

// 리스트에 담기
        ArrayList<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(item1);
        orderItems.add(item2);

        ReflectionTestUtils.setField(orderRequest, "orderItems", orderItems);

        // 🔸 요청 실행 및 검증
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.orderId").isNumber());

        // 🔸 후속 검증 (재고 차감 확인)
        ProductEntity updated = productRepository.findById(dummyProducts.get(0).getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(7L); // 10 - 2 = 8
    }
}
