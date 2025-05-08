package kr.hhplus.be.server.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kr.hhplus.be.server.api.point.ChargePointRequest;
import kr.hhplus.be.server.api.point.UsePointsRequest;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.user.UserEntity;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class PointControllerIntegrationTest extends IntegerationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePoint_success() throws Exception {
        // given
        // 사용자 생성
        List<UserEntity> dummyUser = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(Select.field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        PointEntity dummyPoint = Instancio.of(PointEntity.class)
            .ignore(Select.field(PointEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(PointEntity.class, "userId"), 1L)
            .set(Select.field(PointEntity.class, "balance"), 0L)
            .create();
        PointEntity savedPoint = pointRepository.saveAndFlush(dummyPoint);

        ChargePointRequest request = new ChargePointRequest(savedUser.get(0).getId(), 1000L);
        System.out.println(
            "저장된 유저 ID 목록: " +
                savedUser.stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList())
        );
        // when & then
        mockMvc.perform(post("/api/v1/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.balance").value(1000L));
    }

    @Test
    @DisplayName("포인트 조회 성공")
    void getPoint_success() throws Exception {
        // given: 사전 충전
        PointEntity dummyPoint = Instancio.of(PointEntity.class)
            .set(Select.field(PointEntity.class, "userId"), 2L)
            .set(Select.field(PointEntity.class, "amount"), 5000L)
            .create();

        // when & then
        mockMvc.perform(get("/api/v1/points/userId=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(2L))
            .andExpect(jsonPath("$.data.amount").value(5000L));
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoint_success() throws Exception {
        // given: 사전 충전
        PointEntity dummyPoint = Instancio.of(PointEntity.class)
            .ignore(Select.field(PointEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(PointEntity.class, "userId"), 1L)
            .set(Select.field(PointEntity.class, "balance"), 5000L)
            .create();
        OrderEntity dummyOrder = Instancio.of(OrderEntity.class)
            .ignore(Select.field(OrderEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(OrderEntity.class, "userId"), 1L)
            .set(Select.field(OrderEntity.class, "totalAmount"), 3000L)
            .set(Select.field(OrderEntity.class, "status"), PaymentStatus.NOT_PAID)
            .set(Select.field(OrderEntity.class, "orderProduct"), new ArrayList<>())
            .create();

        PointEntity savedPoint = pointRepository.saveAndFlush(dummyPoint);
        OrderEntity savedOrder = orderRepository.saveAndFlush(dummyOrder);
        // given
        UsePointsRequest request = new UsePointsRequest(1L);
        System.out.println("Saved ID: " + dummyOrder.getId());
        // when & then
        // 외부 전송 mock
        when(restTemplate.postForEntity(
            anyString(), any(HttpEntity.class), eq(String.class))
        ).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        mockMvc.perform(post("/api/v1/points/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @DisplayName("포인트 충전 동시성 테스트")
    void chargePoint_concurrent() throws Exception {
        int threadCount = 5; // 10개의 쓰레드로 동시에 요청
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 3개 쓰레드 풀 / 3개의 쓰레드를 3개씩 실행
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 요청이 완료될 때까지 기다리게 함

        // 사용자 생성
        List<UserEntity> dummyUser = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(UserEntity.class)
                .ignore(Select.field(UserEntity.class, "id"))
                .create())
            .toList();

        List<UserEntity> savedUser = userRepository.saveAll(dummyUser);

        PointEntity dummyPoint = Instancio.of(PointEntity.class)
            .ignore(Select.field(PointEntity.class, "id"))  // supply → ignore 로 변경
            .set(Select.field(PointEntity.class, "userId"), 1L)
            .set(Select.field(PointEntity.class, "balance"), 0L)
            .create();
        PointEntity savedPoint = pointRepository.saveAndFlush(dummyPoint);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ChargePointRequest request = new ChargePointRequest(savedUser.get(0).getId(), 100L); // 1번 유저에게 100포인트 충전

                    mockMvc.perform(post("/api/v1/points/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown(); // 하나 끝날 때마다 카운트 감소
                }
            });
        }

        latch.await(); // 모든 요청이 완료될 때까지 대기
        Optional<PointEntity> point = pointRepository.findByUserId(savedUser.get(0).getId());
        System.out.println(point.get().getBalance());

        // 이후 실제 유저 포인트를 조회해서 100 * 100 = 10,000 포인트 충전되었는지 검증
        mockMvc.perform(get("/api/v1/points/userId=1", savedUser.get(0).getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balance").value(500L));
    }
}
