package kr.hhplus.be.server.product;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.List;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.domain.product.BestSellerEntity;
import kr.hhplus.be.server.domain.product.BestSellerRepository;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ServerApplication.class)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BestSellerRepository bestSellerRepository;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        List<BestSellerEntity> dummyList = Instancio.ofList(BestSellerEntity.class)
            .size(5)
            .create();

        bestSellerRepository.saveAll(dummyList);

        List<ProductEntity> productDummyList = Instancio.ofList(ProductEntity.class)
            .size(20)
            .create();

        productRepository.saveAll(productDummyList);
    }
    @Test
    void bestSeller_Read() throws Exception {
        mockMvc.perform(get("/api/v1/products/best"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))  // 성공 코드 확인
            .andExpect(jsonPath("$.data").isArray())         // 응답이 배열인지 확인
            .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(5))); // 5개 이하인지 확인
    }

    @Test
    void productList_Read() throws Exception {
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