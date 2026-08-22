package com.project.hugme.domain.notification.client;

import com.project.hugme.domain.notification.dto.KakaoMessageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KakaoMessageClient {

    private static final String HUGME_URL =
            "https://hugm3.com";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KakaoMessageClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://kapi.kakao.com")
                .build();
        this.objectMapper = objectMapper;
    }

    public void sendToMe(
            String accessToken,
            String message
    ) {
        String templateObject =
                createTemplateObject(message);

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("template_object", templateObject);

        KakaoMessageResponse response = restClient
                .post()
                .uri("/v2/api/talk/memo/default/send")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoMessageResponse.class);

        if (response == null
                || !Integer.valueOf(0).equals(response.resultCode())) {
            throw new IllegalStateException(
                    "카카오톡 메시지 전송에 실패했습니다."
            );
        }
    }

    private String createTemplateObject(
            String message
    ) {
        Map<String, Object> link =
                new LinkedHashMap<>();

        link.put("web_url", HUGME_URL);
        link.put("mobile_web_url", HUGME_URL);

        Map<String, Object> template =
                new LinkedHashMap<>();

        template.put("object_type", "text");
        template.put("text", message);
        template.put("link", link);
        template.put("button_title", "HUGME 바로가기");

        try {
            return objectMapper.writeValueAsString(template);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "카카오 메시지 내용을 생성하지 못했습니다.",
                    exception
            );
        }
    }
}
