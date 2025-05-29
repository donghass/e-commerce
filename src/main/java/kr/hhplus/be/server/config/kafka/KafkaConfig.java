package kr.hhplus.be.server.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.util.HashMap;
import java.util.Map;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {
    private final ObjectMapper objectMapper; // JacksonConfig에서 등록된 것을 주입받음

    public KafkaConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // DLQ Recoverer 등록
    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLQ", record.partition())
        );
    }

    // 에러 핸들러 등록
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2)); // 재시도 2번 후 DLQ
    }

    // KafkaListener에 위 에러핸들러 연결
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
        ConsumerFactory<Object, Object> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler); // DLQ 처리
        return factory;
    }

    @Bean
    public ConsumerFactory<Object, Object> defaultConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 쿠폰 발급용 (3개 컨슈머)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponIssueCommand> couponKafkaListenerContainerFactory(
        ConsumerFactory<String, CouponIssueCommand> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, CouponIssueCommand> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(couponConsumerFactory());
        factory.setCommonErrorHandler(errorHandler); // DLQ 처리
        factory.setConcurrency(3);
        // 수동 ack 모드 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
    @Bean
    public ConsumerFactory<String, CouponIssueCommand> couponConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "coupon-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CouponIssueCommand.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
