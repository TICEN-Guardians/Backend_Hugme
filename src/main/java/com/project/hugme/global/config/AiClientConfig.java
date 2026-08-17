package com.project.hugme.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Bean
    public RestClient aiRestClient(
            @Value("${ai.base-url}") String aiBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(aiBaseUrl)
                .build();
    }
}