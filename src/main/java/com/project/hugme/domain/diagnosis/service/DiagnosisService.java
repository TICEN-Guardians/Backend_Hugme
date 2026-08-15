package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.infra.ai.diagnosis.FastApiDiagnosisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final FastApiDiagnosisClient fastApiDiagnosisClient;

    public PropertyResolveResponse resolveProperty(
            PropertyResolveRequest request
    ) {
        FastApiPropertyResolveRequest fastApiRequest =
                FastApiPropertyResolveRequest.from(
                        request.address()
                );

        FastApiPropertyResolveResponse fastApiResponse =
                fastApiDiagnosisClient.resolveProperty(
                        fastApiRequest
                );

        return fastApiResponse.toResponse();
    }
}