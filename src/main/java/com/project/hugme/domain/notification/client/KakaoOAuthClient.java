package com.project.hugme.domain.notification.client;

import com.project.hugme.domain.notification.config.KakaoMessageProperties;
import com.project.hugme.domain.notification.dto.KakaoTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient {

    private final KakaoMessageProperties properties;
    private final RestClient restClient;

    public KakaoOAuthClient(
            KakaoMessageProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl("https://kauth.kakao.com")
                .build();
    }

    public KakaoTokenResponse issueToken(
            String authorizationCode
    ) {
        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("redirect_uri", properties.getRedirectUri());
        body.add("code", authorizationCode);

        KakaoTokenResponse response = restClient
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoTokenResponse.class);

        if (response == null
                || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException(
                    "카카오 액세스 토큰을 발급받지 못했습니다."
            );
        }

        return response;
    }
}
