package kr.hhplus.be.server.api.point;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[포인트]", description = "포인트 관련 API 입니다.")
public interface PointControllerDocs {

    @Operation(
        summary = "포인트 조회 API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "포인트 조회 성공",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SUCCESS", description = "코드")),
                        @SchemaProperty(name = "data", schema = @Schema(implementation = PointResponse.class))
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
    public ResponseEntity<CommonResponse<PointResponse>> getPoint(@PathVariable Long userId);

    @Operation(
        summary = "포인트 충전 API",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "포인트 충전 성공",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "SUCCESS", description = "코드")),
                        @SchemaProperty(name = "data", schema = @Schema(implementation = PointResponse.class))
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
                description = "금액이 0 이하인 경우",
                content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schemaProperties = {
                        @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "BAD_PARAM", description = "코드")),
                        @SchemaProperty(name = "msg", schema = @Schema(type = "string", example = "금액은 0보다 커야 합니다.", description = "메세지"))
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
    public ResponseEntity<CommonResponse<PointResponse>> charge(@RequestBody ChargePointRequest request);
}
