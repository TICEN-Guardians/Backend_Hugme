package com.project.hugme.infra.ai.diagnosis;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiAddressSuggestionRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiAddressSuggestionResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiRegistryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class FastApiDiagnosisClient {
    private final RestClient restClient;

    public FastApiDiagnosisClient(
            @Qualifier("diagnosisRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public FastApiAddressSuggestionResponse suggestAddress(
            FastApiAddressSuggestionRequest request
    ) {
        FastApiAddressSuggestionResponse response = restClient.post()
                .uri("/internal/v1/properties/suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiAddressSuggestionResponse.class);
        if (response == null) {
            throw new IllegalStateException("FastAPI 주소 제안 응답이 없습니다.");
        }
        return response;
    }

    public FastApiPropertySearchResponse searchProperty(
            FastApiPropertySearchRequest request
    ) {
        FastApiPropertySearchResponse response = restClient.post()
                .uri("/internal/v1/properties/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiPropertySearchResponse.class);
        if (response == null) {
            throw new IllegalStateException("FastAPI 주소 검색 응답이 없습니다.");
        }
        return response;
    }

    public FastApiPropertyResolveResponse resolveProperty(
            FastApiPropertyResolveRequest request
    ) {
        FastApiPropertyResolveResponse response = restClient.post()
                .uri("/internal/v1/properties/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiPropertyResolveResponse.class);
        if (response == null) {
            throw new IllegalStateException("FastAPI 주소 확인 응답이 없습니다.");
        }
        return response;
    }

    public FastApiDiagnosisResponse analyze(FastApiDiagnosisRequest request) {
        FastApiDiagnosisResponse response = restClient.post()
                .uri("/internal/v1/diagnoses/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FastApiDiagnosisResponse.class);
        if (response == null) {
            throw new IllegalStateException("FastAPI 진단 응답이 없습니다.");
        }
        return response;
    }

    public FastApiRegistryResponse uploadRegistry(
            Long analysisId,
            List<MultipartFile> files
    ) {
        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("analysis_id", analysisId.toString());
            for (MultipartFile file : files) {
                body.part("files", resource(file))
                        .contentType(MediaType.APPLICATION_PDF);
            }
            FastApiRegistryResponse response = restClient.post()
                    .uri("/register/check")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(FastApiRegistryResponse.class);
            if (response == null) {
                throw new IllegalStateException("등기 OCR 응답이 없습니다.");
            }
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "등기부등본 파일을 읽을 수 없습니다.",
                    exception
            );
        }
    }

    private ByteArrayResource resource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }
}
