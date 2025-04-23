package kr.hhplus.be.server.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.coupon.CouponIssueRequest;
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
import kr.hhplus.be.server.domain.user.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() throws Exception {
        // 사용자 생성
        List<UserEntity> dummyUser = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(Select.field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);
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

    @Test
    @DisplayName("주문 재고 차감 동시성 테스트")
    void createOrder_concurrent_stockLimit() throws Exception {
        // 10명의 사용자 생성
        List<UserEntity> users = IntStream.range(0, 10)
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(Select.field(UserEntity.class, "id"))
                .create())
            .toList();
        List<UserEntity> savedUsers = userRepository.saveAll(users);

        // 재고 10인 상품 등록
        ProductEntity product = Instancio.of(ProductEntity.class)
            .ignore(Select.field(ProductEntity.class, "id"))
            .set(Select.field(ProductEntity.class, "stock"), 10L)
            .create();
        ProductEntity savedProduct = productRepository.save(product);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    // OrderItem 생성
                    OrderRequest.OrderItem item = new OrderRequest.OrderItem();
                    item.setProductId(savedProduct.getId());
                    item.setQuantity(1L);

                    OrderRequest request = new OrderRequest();

                    Field userIdField = OrderRequest.class.getDeclaredField("userId");
                    userIdField.setAccessible(true);
                    userIdField.set(request, savedUsers.get(finalI).getId());

                    Field orderItemsField = OrderRequest.class.getDeclaredField("orderItems");
                    orderItemsField.setAccessible(true);
                    orderItemsField.set(request, new ArrayList<>(List.of(item)));

                    mockMvc.perform(post("/api/v1/orders")
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

        // 검증
        ProductEntity updated = productRepository.findById(savedProduct.getId()).orElseThrow();

        System.out.println("남은 재고 = " + updated.getStock());

        Assertions.assertEquals(0, updated.getStock(), "모든 재고가 소진되어야 합니다.");
    }
}
