package kr.hhplus.be.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.point.ChargePointRequest;
import kr.hhplus.be.server.api.point.UsePointsRequest;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEntity.PaymentStatus;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
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

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePoint_success() throws Exception {
        // given
        ChargePointRequest request = new ChargePointRequest(1L, 1000L);

        // when & then
        mockMvc.perform(post("/api/v1/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.amount").value(1000L));
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
}
