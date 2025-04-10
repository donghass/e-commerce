package kr.hhplus.be.server.api.order;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[주문]", description = "주문 관련 API 입니다.")
public interface OrderControllerDocs {

    @Operation(
        summary = "주문/결제 API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "주문/결제 성공",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SUCCESS", description = "코드")),
                        @SchemaProperty(name = "data", schema = @Schema(implementation = OrderResponse.class))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잔고가 부족한 경우",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "NOT_ENOUGH_MONEY", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "잔고가 부족합니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400 ",
                description = "쿠폰이 만료된 경우",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "EXPIRE_COUPON", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "만료된 쿠폰입니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400  ",
                description = "상품 재고가 부족한 경우",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "INSUFFICIENT_STOCK", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "상품 재고가 부족합니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 에러 발생",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SERVER_ERROR", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "예기치 못한 오류가 발생하였습니다.", description = "메세지"))
                    }
                )
            )
        }
    )
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request);
}
