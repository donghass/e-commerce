package kr.hhplus.be.server.config.jpa.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private int id;
    private String name;
    private long price;
    private int stock;
}
