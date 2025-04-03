package kr.hhplus.be.server;

import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
public class PointAPITest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void 포인트_충전_성공() throws Exception {
        mockMvc.perform(post("/api/v1/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "userId": 1,
                    "chargeAmount": 100000
                }
            """))
            .andExpect(status().isOk());
    }
}
