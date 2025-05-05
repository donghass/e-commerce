package kr.hhplus.be.server.product;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    void bestSeller_Read() throws Exception {
        List<BestSellerEntity> dummyBestSellerList = IntStream.range(0, 5) // 원하는 개수만큼 생성
            .mapToObj(i -> Instancio.of(BestSellerEntity.class)
                .ignore(Select.field(BestSellerEntity.class, "id"))
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
                .ignore(Select.field(ProductEntity.class, "id"))
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
}