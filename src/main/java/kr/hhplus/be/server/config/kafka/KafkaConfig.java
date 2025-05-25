package kr.hhplus.be.server.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {
    private final ObjectMapper objectMapper; // JacksonConfig에서 등록된 것을 주입받음

    public KafkaConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
