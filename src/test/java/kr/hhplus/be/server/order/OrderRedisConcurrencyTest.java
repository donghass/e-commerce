package kr.hhplus.be.server.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.scheduler.OrderScheduler;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class OrderRedisConcurrencyTest extends IntegerationTestSupport {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private OrderScheduler orderScheduler;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("주문 생성 및 주문 취소 레디스 분산락 동시성 테스트")
    public void orderCreationAndCancellationWithConcurrency() throws InterruptedException {
        // given
        // 사용자 생성
        UserEntity dummyUser = Instancio.of(UserEntity.class)
            .ignore(Select.field(UserEntity.class, "id"))
            .create();

        UserEntity savedUser = userRepository.save(dummyUser);
        // 현재 시간에서 10분을 뺀 시간 (10분 전)
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);

        // 준비: 더미 주문 생성
        OrderEntity dummyOrders = Instancio.of(OrderEntity.class)
            .ignore(field(OrderEntity.class, "id"))
            .set(field(OrderEntity.class, "createdAt"), tenMinutesAgo)
            .set(field(OrderEntity.class, "userCouponId"), null)
            .set(field(OrderEntity.class, "userId"), savedUser.getId()) // userId 설정
            .create();
        // 주문 저장
        OrderEntity savedOrders = orderRepository.saveAndFlush(dummyOrders);  // 주문은 그 후에 저장
        entityManager.flush(); // flush를 통해 영속성 컨텍스트에 반영
        // 더미 주문 상품 생성
        List<OrderProductEntity> dummyOrderProduct = IntStream.range(0, 2) // 50개의 주문 생성
            .mapToObj(i -> Instancio.of(OrderProductEntity.class)
                .ignore(field(OrderProductEntity.class, "id"))
                .set(field(OrderProductEntity.class, "productId"), (long) (i + 1))
                .set(field(OrderProductEntity.class, "order"), savedOrders) // order 필드에 dummyOrders 설정
                .set(field(OrderProductEntity.class, "quantity"), 1L)
                .create())
            .toList();

        orderRepository.saveAll(dummyOrderProduct);

        List<ProductEntity> dummyProduct = IntStream.range(0, 1) // 1개의 상품 생성
            .mapToObj(i -> Instancio.of(ProductEntity.class)
                .ignore(field(ProductEntity.class, "id"))
                .set(Select.field(ProductEntity.class, "stock"), 10L)
                .create())
            .toList();

        List<ProductEntity> savedProduct = productRepository.saveAll(dummyProduct);

        int numberOfThreads = 5; // 동시 실행할 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Boolean>> futures = new ArrayList<>();
        // when
        // 실행: 주문 생성 및 취소를 위한 동시성 작업 제출
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    // 주문 생성
                    // 1. OrderProduct 객체 생성
                    OrderCommand.OrderProduct orderProduct1 = new OrderCommand.OrderProduct(1L, 2L); // 제품 ID 1번, 수량 2
                    OrderCommand.OrderProduct orderProduct2 = new OrderCommand.OrderProduct(2L, 3L); // 제품 ID 2번, 수량 3

                    // 2. OrderCommand 객체 생성
                    List<OrderCommand.OrderProduct> orderProducts = List.of(orderProduct1, orderProduct2);
                    OrderCommand orderCommand = new OrderCommand(1L, null, orderProducts); // 사용자 ID 1,  주문 항목 리스트


                    OrderResult orderResult = orderFacade.createOrder(orderCommand);

                    // 주문 취소 (동일 주문 ID로 취소)
                    orderScheduler.checkAndExpireOrders();

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

        // 성공 횟수 카운트
        long successCount = futures.stream()
            .filter(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    return false;
                }
            })
            .count();

        // then
        assertThat(successCount).isEqualTo(1L); // 성공 횟수는 1번이어야 함 (동시성 락 테스트의 결과)
        System.out.println("주문 생성 및 취소 성공 횟수 → " + successCount);
    }
}