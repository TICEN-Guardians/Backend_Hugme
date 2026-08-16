package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.repository.DiagnosisRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.repository.UserRepository;
import com.project.hugme.infra.ai.diagnosis.FastApiDiagnosisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.request.PropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisService {

    private final FastApiDiagnosisClient fastApiDiagnosisClient;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;

    public PropertySearchResponse searchProperty(
            PropertySearchRequest request
    ) {
        FastApiPropertySearchRequest fastApiRequest =
                FastApiPropertySearchRequest.from(request);

        FastApiPropertySearchResponse fastApiResponse =
                fastApiDiagnosisClient.searchProperty(
                        fastApiRequest
                );

        return fastApiResponse.toResponse();
    }

    public PropertyResolveResponse resolveProperty(PropertyResolveRequest request) {

        FastApiPropertyResolveRequest fastApiRequest =
                FastApiPropertyResolveRequest.from(request);

        FastApiPropertyResolveResponse fastApiResponse =
                fastApiDiagnosisClient.resolveProperty(fastApiRequest);

        return fastApiResponse.toResponse();
    }
    @Transactional
    public DiagnosisCreateResponse createDiagnosis(
            Long userId,
            DiagnosisCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Diagnosis diagnosis = Diagnosis.create(
                user,
                request.address(),
                request.deposit(),
                request.contractDate(),
                request.contractArea()
        );

        Diagnosis savedDiagnosis =
                diagnosisRepository.save(diagnosis);

        return DiagnosisCreateResponse.from(savedDiagnosis);
    }
}