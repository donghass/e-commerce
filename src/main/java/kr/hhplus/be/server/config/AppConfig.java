package kr.hhplus.be.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    @ConditionalOnMissingBean  // 테스트에서 Mock 빈이 등록되어 있으면 이건 무시됨
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}