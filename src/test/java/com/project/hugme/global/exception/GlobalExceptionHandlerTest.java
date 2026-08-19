package com.project.hugme.global.exception;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(new ObjectMapper());

    private static RestClientResponseException aiError(
            HttpStatus status,
            String body
    ) {
        return new RestClientResponseException(
                "AI error",
                status.value(),
                status.getReasonPhrase(),
                null,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
    }

    @Test
    void 주소가_여러건이면_AI_메시지를_그대로_전달한다() {

        ResponseEntity<ErrorResponse> response = handler.handleAiResponse(
                aiError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "{\"detail\":{\"code\":\"PROPERTY_RESOLUTION_FAILED\","
                                + "\"message\":\"주소 검색 결과 다수: 4건\"}}"
                )
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(422);
        assertThat(response.getBody().code())
                .isEqualTo("PROPERTY_RESOLUTION_FAILED");
        assertThat(response.getBody().message())
                .isEqualTo("주소 검색 결과 다수: 4건");
    }

    @Test
    void 동_정보가_필요하면_AI_메시지를_그대로_전달한다() {

        ResponseEntity<ErrorResponse> response = handler.handleAiResponse(
                aiError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "{\"detail\":{\"code\":\"PROPERTY_RESOLUTION_FAILED\","
                                + "\"message\":\"공동주택 동 정보 필요\"}}"
                )
        );

        assertThat(response.getBody().message())
                .isEqualTo("공동주택 동 정보 필요");
    }

    @Test
    void AI_내부오류는_502로_바꾸고_원문을_노출하지_않는다() {

        ResponseEntity<ErrorResponse> response = handler.handleAiResponse(
                aiError(
                        HttpStatus.BAD_GATEWAY,
                        "{\"detail\":{\"code\":\"PROPERTY_EXTERNAL_API_ERROR\","
                                + "\"message\":\"주소 API 호출 실패: 500\"}}"
                )
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().code())
                .isEqualTo("AI_SERVICE_ERROR");
        assertThat(response.getBody().message())
                .doesNotContain("주소 API 호출 실패");
    }

    @Test
    void 해석할_수_없는_본문이면_기본_문구를_준다() {

        ResponseEntity<ErrorResponse> response = handler.handleAiResponse(
                aiError(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "{\"detail\":[{\"loc\":[\"body\"],\"msg\":\"field required\"}]}"
                )
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(422);
        assertThat(response.getBody().code())
                .isEqualTo("AI_REQUEST_REJECTED");
        assertThat(response.getBody().message())
                .isEqualTo("요청을 처리할 수 없습니다.");
    }

    @Test
    void 본문이_비어도_기본_문구를_준다() {

        ResponseEntity<ErrorResponse> response =
                handler.handleAiResponse(
                        aiError(HttpStatus.UNPROCESSABLE_ENTITY, "")
                );

        assertThat(response.getBody().code())
                .isEqualTo("AI_REQUEST_REJECTED");
    }

    @Test
    void AI에_연결하지_못하면_504를_준다() {

        ResponseEntity<ErrorResponse> response =
                handler.handleAiUnreachable(
                        new ResourceAccessException("connect timed out")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody().code())
                .isEqualTo("AI_SERVICE_TIMEOUT");
    }
}
