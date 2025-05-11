package kr.hhplus.be.server.product;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.cleanUp.IntegerationTestSupport;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.BestSellerRepository;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.UserEntity;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest extends IntegerationTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BestSellerRepository bestSellerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void bestSeller_Read() throws Exception {
        List<BestSellerEntity> dummyBestSellerList = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(BestSellerEntity.class)
                .ignore(field(BestSellerEntity.class, "id"))
                .create())
            .toList();

        bestSellerRepository.saveAllAndFlush(dummyBestSellerList);
        // ObjectMapper를 사용해서 리스트 출력
        System.out.println("Dummy List: " + objectMapper.writeValueAsString(dummyBestSellerList));

        mockMvc.perform(get("/api/v1/products/best"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))  // 성공 코드 확인
            .andExpect(jsonPath("$.data").isArray())         // 응답이 배열인지 확인
            .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(5))); // 5개 이하인지 확인
    }

    @Test
    void productList_Read() throws Exception {
        List<ProductEntity> productDummyList = IntStream.range(0, 20) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(ProductEntity.class)
                .ignore(field(ProductEntity.class, "id"))
                .create())
            .toList();

        productRepository.saveAll(productDummyList);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "6")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()", lessThanOrEqualTo(5)));
    }

    @Test
    @DisplayName("일간 인기상품 조회 성공")
    void getDailyTopProducts_success() throws Exception {

        List<ProductEntity> dummyProduct = IntStream.range(0, 15) // 15개의 상품 생성
            .mapToObj(i -> Instancio.of(ProductEntity.class)
                .ignore(field(ProductEntity.class, "id"))
                .set(Select.field(ProductEntity.class, "stock"), 10L)
                .create())
            .toList();

        productRepository.saveAll(dummyProduct);

        // Redis 랭킹에 점수 수동 입력
        String key = "ranking:daily:" + LocalDate.now();
        for(int i=0; i<15; i++){
            redisTemplate.opsForZSet().add(key, String.valueOf(i), i);  // 인기 점수
        }
        redisTemplate.expire(key, Duration.ofDays(1));

        // when & then
        mockMvc.perform(get("/api/v1/products/top")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(10))
            .andExpect(jsonPath("$.data[0].id").value(14))  // TOP 1 인기점수 value 값은 id 값
            .andExpect(jsonPath("$.data[9].id").value(5));  // TOP 10 인기점수
    }
}