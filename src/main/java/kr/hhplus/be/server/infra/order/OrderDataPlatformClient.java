package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.domain.order.DataPlatformClient;
import kr.hhplus.be.server.domain.order.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OrderDataPlatformClient implements DataPlatformClient {

    private final RestTemplate restTemplate;

    public void sendToDataPlatform(OrderEntity order) {
        String url = "https://mock-dataplatform.com/api/payments";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OrderEntity> entity = new HttpEntity<>(order, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("데이터 플랫폼에 성공적으로 전송됨");
        } else {
            System.err.println("전송 실패: " + response.getStatusCode());
        }
    }
}
