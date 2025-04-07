package kr.hhplus.be.server.api.coupon;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.api.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[쿠폰]", description = "쿠폰 관련 API 입니다.")
public interface CouponControllerDocs {

    @Operation(
        summary = "쿠폰 발급 API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "쿠폰 발급 성공",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SUCCESS", description = "코드")),
                        @SchemaProperty(name = "data", schema = @Schema(implementation = CouponResponse.class))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 사용자 식별자",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "BAD_PARAM", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "유효하지 않은 사용자 식별자입니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400 ",
                description = "유효하지 않은 쿠폰 식별자",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "BAD_PARAM", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "유효하지 않은 쿠폰 식별자입니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400  ",
                description = "쿠폰 수량이 부족",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "NOT_ENOUPH_COUPON", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "쿠폰 수량이 부족합니다.", description = "메세지"))
                    }
                )
            ),
            @ApiResponse(
                responseCode = "400   ",
                description = "만료된 쿠폰",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "EXPIRE_COUPON", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "만료된 쿠폰입니다.", description = "메세지"))
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
    public ResponseEntity<CommonResponse<Object>> issueCoupon(@Valid @RequestBody CouponIssueRequest request);
}