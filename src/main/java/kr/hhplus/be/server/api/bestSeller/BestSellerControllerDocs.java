package kr.hhplus.be.server.api.bestSeller;


import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.api.product.ProductListResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "[상품]", description = "상품 관련 API 입니다.")
public interface BestSellerControllerDocs {

//    @Operation(
//        summary = "상위 상품 조회 API",
//        responses = {
//            @ApiResponse(
//                responseCode = "200",
//                description = "상위 상품 조회 성공",
//                content = @Content(
//                    mediaType = APPLICATION_JSON_VALUE,
//                    schemaProperties = {
//                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SUCCESS", description = "코드")),
//                        @SchemaProperty(name = "data", schema = @Schema(implementation = BestSellerProductResponse.class))
//                    }
//                )
//            ),
//            @ApiResponse(
//                responseCode = "500",
//                description = "서버 에러 발생",
//                content = @Content(
//                    mediaType = APPLICATION_JSON_VALUE,
//                    schemaProperties = {
//                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SERVER_ERROR", description = "코드")),
//                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "예기치 못한 오류가 발생하였습니다.", description = "메세지"))
//                    }
//                )
//            )
//        }
//    )
//    public ResponseEntity<CommonResponse<List<ProductListResponse>>> getPopularProducts();
}
