package com.project.hugme.domain.notification.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KakaoMessageProperties {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public KakaoMessageProperties(
            @Value("${kakao.message.client-id}")
            String clientId,

            @Value("${kakao.message.client-secret}")
            String clientSecret,

            @Value("${kakao.message.redirect-uri}")
            String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }
}