package com.project.hugme.infra.ai.diagnosis;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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

    public FastApiDiagnosisResponse analyze(
            FastApiDiagnosisRequest request
    ) {
        FastApiDiagnosisResponse response = restClient
                .post()
                .uri("/internal/v1/diagnoses/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiDiagnosisResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "FastAPI 진단 응답이 없습니다."
            );
        }

        return response;
    }

    public void uploadRegistry(
            Long analysisId,
            MultipartFile file
    ) {
        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("analysis_id", analysisId.toString());
            body.part("files", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }).contentType(MediaType.APPLICATION_PDF);

            restClient.post()
                    .uri("/register/check")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(
                    "등기부등본 파일을 읽을 수 없습니다.",
                    exception
            );
        }
    }
}
