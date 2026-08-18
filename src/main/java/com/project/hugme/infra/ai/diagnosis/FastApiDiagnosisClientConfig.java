package com.project.hugme.infra.ai.diagnosis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FastApiDiagnosisClientConfig {

    @Bean
    public RestClient diagnosisRestClient(
            RestClient.Builder builder,
            @Value("${hugme.ai.base-url}") String baseUrl,
            @Value("${hugme.ai.connect-timeout:10s}") Duration connectTimeout,
            @Value("${hugme.ai.read-timeout:300s}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
