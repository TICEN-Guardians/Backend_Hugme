package com.project.hugme.infra.ai.diagnosis;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchResponse;

@Component
public class FastApiDiagnosisClient {

    private final RestClient restClient;

    public FastApiDiagnosisClient(
            @Qualifier("diagnosisRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public FastApiPropertySearchResponse searchProperty(
            FastApiPropertySearchRequest request
    ) {
        FastApiPropertySearchResponse response = restClient
                .post()
                .uri("/internal/v1/properties/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiPropertySearchResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "FastAPI 주소 검색 응답이 없습니다."
            );
        }

        return response;
    }

    public FastApiPropertyResolveResponse resolveProperty(
            FastApiPropertyResolveRequest request
    ) {
        FastApiPropertyResolveResponse response = restClient
                .post()
                .uri("/internal/v1/properties/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiPropertyResolveResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "FastAPI 주소 확인 응답이 없습니다."
            );
        }

        return response;
    }
}