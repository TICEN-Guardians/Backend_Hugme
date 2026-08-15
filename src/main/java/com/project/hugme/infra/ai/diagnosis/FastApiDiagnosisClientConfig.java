package com.project.hugme.infra.ai.diagnosis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FastApiDiagnosisClientConfig {

    @Bean
    public RestClient diagnosisRestClient(
            RestClient.Builder builder,
            @Value("${hugme.ai.base-url}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}