package kr.hhplus.be.server.config.jpa;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Mock API Documentation")
                .version("1.0")
                .description("This is the API documentation for the Mock API project.")
                .termsOfService("Terms of service")
                .contact(new Contact().name("Your Name").email("your-email@example.com")));
    }
}