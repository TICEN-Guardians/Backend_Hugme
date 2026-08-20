package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiRegistryResponse;
import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisDetailsRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisReportResponse;
import com.project.hugme.domain.diagnosis.dto.response.RegistryOcrResponse;
import com.project.hugme.domain.diagnosis.dto.response.RegistrySummaryResponse;
import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.entity.DiagnosisResult;
import com.project.hugme.domain.diagnosis.repository.DiagnosisRepository;
import com.project.hugme.domain.diagnosis.repository.DiagnosisResultRepository;
import com.project.hugme.domain.diagnosis.enums.HousingType;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.repository.UserRepository;
import com.project.hugme.infra.ai.diagnosis.FastApiDiagnosisClient;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.repository.LandlordWatchlistCheckRepository;
import com.project.hugme.infra.ocr.repository.RegistryResultRepository;
import com.project.hugme.infra.ocr.repository.RegistryOwnerRepository;
import com.project.hugme.infra.ocr.repository.RegistryRightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.request.PropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertySearchResponse;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisService {

    private static final ObjectMapper JSON_MAPPER =
            new ObjectMapper().findAndRegisterModules();

    private final FastApiDiagnosisClient fastApiDiagnosisClient;
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final UserRepository userRepository;
    private final RegistryResultRepository registryResultRepository;
    private final RegistryOwnerRepository registryOwnerRepository;
    private final RegistryRightRepository registryRightRepository;
    private final LandlordWatchlistCheckRepository watchlistCheckRepository;

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
                request.dongName(),
                request.hoName(),
                request.deposit(),
                request.contractDate(),
                request.contractArea(),
                request.exclusiveArea(),
                request.floor(),
                request.landlordName(),
                writePropertySnapshot(request.propertySnapshot())
        );

        Diagnosis savedDiagnosis =
                diagnosisRepository.save(diagnosis);

        return DiagnosisCreateResponse.from(savedDiagnosis);
    }

    @Transactional
    public void updateDetails(Long userId, Long analysisId, DiagnosisDetailsRequest request) {
        Diagnosis diagnosis = diagnosisRepository.findByAnalysisIdAndUserUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException("진단 요청을 찾을 수 없습니다."));
        diagnosis.updateDetails(request.dongName(), request.hoName(), request.deposit(),
                request.contractDate(), request.contractArea(), request.exclusiveArea(),
                request.floor(), request.landlordName());
    }
    @Transactional
    public FastApiDiagnosisResponse analyzeDiagnosis(
            Long userId,
            Long analysisId
    ) {
        Diagnosis diagnosis = diagnosisRepository
                .findByAnalysisIdAndUserUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "진단 요청을 찾을 수 없습니다."
                ));
        RegistryResult registryResult = registryResultRepository
                .findTopByAnalysisIdOrderByParsedAtDesc(analysisId)
                .orElse(null);
        List<LandlordWatchlistCheck> checks = registryResult == null
                ? List.of()
                : watchlistCheckRepository
                        .findByRegistryResultRegistryResultIdOrderByCheckIdAsc(
                                registryResult.getRegistryResultId()
                        );
        List<RegistryOwner> owners = registryResult == null
                ? List.of()
                : registryOwnerRepository
                        .findByRegistryResultRegistryResultIdOrderByRegistryOwnerIdAsc(
                                registryResult.getRegistryResultId()
                        );

        diagnosis.markAnalyzing();
        FastApiDiagnosisResponse response = fastApiDiagnosisClient.analyze(
                FastApiDiagnosisRequest.from(
                        diagnosis,
                        registryResult,
                        checks,
                        owners,
                        readPropertySnapshot(diagnosis.getPropertySnapshot())
                )
        );
        diagnosis.complete(
                response.property().normalizedAddress(),
                HousingType.valueOf(response.property().housingType())
        );
        saveResult(diagnosis, response);
        return response;
    }

    public DiagnosisReportResponse getDiagnosisResult(
            Long userId,
            Long analysisId
    ) {
        diagnosisRepository
                .findByAnalysisIdAndUserUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "진단 요청을 찾을 수 없습니다."
                ));
        DiagnosisResult result = diagnosisResultRepository
                .findByDiagnosisAnalysisId(analysisId)
                .orElseThrow(() -> new IllegalStateException(
                        "완료된 진단 결과가 없습니다."
                ));

        FastApiDiagnosisResponse diagnosis;
        try {
            diagnosis = JSON_MAPPER.readValue(
                    result.getResponseJson(),
                    FastApiDiagnosisResponse.class
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "저장된 진단 결과를 읽을 수 없습니다.",
                    exception
            );
        }

        return DiagnosisReportResponse.of(
                diagnosis,
                findRegistrySummary(analysisId)
        );
    }

    private RegistrySummaryResponse findRegistrySummary(Long analysisId) {
        return registryResultRepository
                .findTopByAnalysisIdOrderByParsedAtDesc(analysisId)
                .map(registryResult -> RegistrySummaryResponse.from(
                        registryResult,
                        registryRightRepository
                                .findByRegistryResultRegistryResultIdOrderByRightIdAsc(
                                        registryResult.getRegistryResultId()
                                )
                ))
                .orElse(null);
    }

    public RegistryOcrResponse uploadRegistry(
            Long userId,
            Long analysisId,
            MultipartFile file
    ) {
        diagnosisRepository
                .findByAnalysisIdAndUserUserId(analysisId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "진단 요청을 찾을 수 없습니다."
                ));
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "등기부등본 PDF 파일이 필요합니다."
            );
        }
        FastApiRegistryResponse response = fastApiDiagnosisClient.uploadRegistry(analysisId, file);
        return response.toResponse();
    }

    /**
     * 매물 스냅샷은 공공 API 재조회를 줄이기 위한 부가 정보다.
     * 변환에 실패하면 저장하지 않고, AI가 예전처럼 다시 조회하게 둔다.
     */
    private String writePropertySnapshot(Map<String, Object> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return null;
        }

        try {
            return JSON_MAPPER.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            log.warn("매물 스냅샷을 저장하지 못했습니다", exception);
            return null;
        }
    }

    private Map<String, Object> readPropertySnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }

        try {
            return JSON_MAPPER.readValue(
                    snapshotJson,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            log.warn("저장된 매물 스냅샷을 읽지 못했습니다", exception);
            return null;
        }
    }

    private void saveResult(
            Diagnosis diagnosis,
            FastApiDiagnosisResponse response
    ) {
        try {
            String responseJson = JSON_MAPPER.writeValueAsString(response);
            DiagnosisResult result = diagnosisResultRepository
                    .findByDiagnosisAnalysisId(diagnosis.getAnalysisId())
                    .orElseGet(() -> DiagnosisResult.create(
                            diagnosis,
                            response,
                            responseJson
                    ));
            result.update(response, responseJson);
            diagnosisResultRepository.save(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "진단 결과 저장 형식 변환에 실패했습니다.",
                    exception
            );
        }
    }
}

