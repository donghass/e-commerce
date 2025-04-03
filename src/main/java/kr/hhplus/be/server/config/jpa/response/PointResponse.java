package kr.hhplus.be.server.config.jpa.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse<T> {
        private Long userId;
        private Long balance;

}
