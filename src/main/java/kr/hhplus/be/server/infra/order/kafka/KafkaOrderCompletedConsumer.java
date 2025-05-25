package kr.hhplus.be.server.infra.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderEvent.OrderCompletedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaOrderCompletedConsumer {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public static final String DATA_PLATFORM_URL = "https://mock-dataplatform.com/api/payments";

    public KafkaOrderCompletedConsumer(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "order.completed", groupId = "data-platform-group")
    public void listen(String message) {
        try {

            OrderCompletedEvent event = objectMapper.readValue(message, OrderCompletedEvent.class);

            // 데이터 플랫폼으로 전송하는 로직 실행
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OrderEntity> entity = new HttpEntity<>(event.order(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(DATA_PLATFORM_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("데이터 플랫폼에 성공적으로 전송됨");
            } else {
                System.err.println("전송 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
