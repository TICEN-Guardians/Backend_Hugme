package com.project.hugme.domain.diagnosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiAddressSuggestionRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiAddressSuggestionResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisWhatIfRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisWhatIfResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertyResolveResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiPropertySearchResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiRegistryResponse;
import com.project.hugme.domain.diagnosis.dto.request.AddressSuggestionRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisAddressRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisDetailsRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisWhatIfRequest;
import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.request.PropertySearchRequest;
import com.project.hugme.domain.diagnosis.dto.response.*;
import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.entity.DiagnosisResult;
import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import com.project.hugme.domain.diagnosis.enums.DiagnosisStatus;
import com.project.hugme.domain.diagnosis.enums.HousingType;
import com.project.hugme.domain.diagnosis.enums.RegistryAddressMatchStatus;
import com.project.hugme.domain.diagnosis.exception.DiagnosisException;
import com.project.hugme.domain.diagnosis.repository.DiagnosisRepository;
import com.project.hugme.domain.diagnosis.repository.DiagnosisResultRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.repository.UserRepository;
import com.project.hugme.infra.ai.diagnosis.FastApiDiagnosisClient;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryRight;
import com.project.hugme.infra.ocr.enums.ParseStatus;
import com.project.hugme.infra.ocr.repository.LandlordWatchlistCheckRepository;
import com.project.hugme.infra.ocr.repository.RegistryOwnerRepository;
import com.project.hugme.infra.ocr.repository.RegistryResultRepository;
import com.project.hugme.infra.ocr.repository.RegistryRightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final DiagnosisAccessTokenService accessTokenService;
    private final RegistryAddressMatchService registryAddressMatchService;

    public AddressSuggestionResponse suggestAddress(
            AddressSuggestionRequest request
    ) {
        FastApiAddressSuggestionResponse response =
                fastApiDiagnosisClient.suggestAddress(
                        FastApiAddressSuggestionRequest.from(request)
                );
        return response.toResponse();
    }

    public PropertySearchResponse searchProperty(PropertySearchRequest request) {
        FastApiPropertySearchRequest fastApiRequest =
                FastApiPropertySearchRequest.from(request);
        FastApiPropertySearchResponse fastApiResponse =
                fastApiDiagnosisClient.searchProperty(fastApiRequest);
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
        if (userId == null && request.mode() == DiagnosisMode.DETAILED) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "DETAILED_DIAGNOSIS_LOGIN_REQUIRED",
                    "정밀진단은 로그인 후 이용할 수 있습니다."
            );
        }
        User user = userId == null
                ? null
                : userRepository.findById(userId)
                        .orElseThrow(UserNotFoundException::new);
        DiagnosisAccessTokenService.IssuedToken token = user == null
                ? accessTokenService.issue()
                : null;
        Diagnosis diagnosis = Diagnosis.create(
                user,
                request.mode(),
                token == null ? null : token.hash(),
                token == null ? null : token.expiresAt()
        );
        Diagnosis saved = diagnosisRepository.save(diagnosis);
        return DiagnosisCreateResponse.from(
                saved,
                token == null ? null : token.value()
        );
    }

    @Transactional
    public void confirmAddress(
            Long userId,
            Long analysisId,
            String accessToken,
            DiagnosisAddressRequest request
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, accessToken);
        ensureEditable(diagnosis);
        RegistryAddressValidation registryValidation = validateRegistryAddressIfPresent(
                diagnosis,
                analysisId,
                request.address(),
                request.dongName(),
                request.hoName(),
                request.propertySnapshot(),
                request.registryAddressReviewConfirmed()
        );
        diagnosis.updateAddress(
                request.address(),
                request.dongName(),
                request.hoName(),
                writePropertySnapshot(request.propertySnapshot()),
                registryValidation.reviewConfirmed()
        );
        diagnosis.markAddressConfirmed(registryValidation.registryReady());
    }

    @Transactional
    public void updateDetails(
            Long userId,
            Long analysisId,
            String accessToken,
            DiagnosisDetailsRequest request
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, accessToken);
        ensureEditable(diagnosis);
        validateDetails(diagnosis, request);
        RegistryAddressValidation registryValidation = validateRegistryAddressIfPresent(
                diagnosis,
                analysisId,
                request.address(),
                request.dongName(),
                request.hoName(),
                request.propertySnapshot(),
                request.registryAddressReviewConfirmed()
        );
        diagnosis.updateDetails(
                request.address(),
                request.dongName(),
                request.hoName(),
                request.deposit(),
                request.contractDate(),
                request.contractArea(),
                request.exclusiveArea(),
                request.floor(),
                normalizeOptional(request.landlordName()),
                writePropertySnapshot(request.propertySnapshot()),
                registryValidation.reviewConfirmed()
        );
        diagnosis.markDetailsReady(registryValidation.registryReady());
    }

    @Transactional
    public RegistryOcrResponse uploadRegistry(
            Long userId,
            Long analysisId,
            List<MultipartFile> files
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, null);
        ensureEditable(diagnosis);
        if (diagnosis.getMode() != DiagnosisMode.DETAILED) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "REGISTRY_NOT_ALLOWED_FOR_QUICK_DIAGNOSIS",
                    "간편진단에는 등기부등본을 첨부할 수 없습니다."
            );
        }
        validateRegistryFiles(files);
        FastApiRegistryResponse response =
                fastApiDiagnosisClient.uploadRegistry(analysisId, files);
        FastApiRegistryResponse.OwnerInfo ownerInfo = response.ownerInfo();
        RegistryAddressMatchStatus addressMatchStatus = registryAddressMatchService.match(
                diagnosis.getAddress(),
                diagnosis.getDongName(),
                diagnosis.getHoName(),
                readPropertySnapshot(diagnosis.getPropertySnapshot()),
                ownerInfo == null ? null : ownerInfo.propertyAddress(),
                ownerInfo == null ? null : ownerInfo.dongName(),
                ownerInfo == null ? null : ownerInfo.hoName()
        );
        boolean successful = response.ownerInfo() != null
                && "SUCCESS".equalsIgnoreCase(response.ownerInfo().parseStatus())
                && (addressMatchStatus == RegistryAddressMatchStatus.MATCH
                || addressMatchStatus
                == RegistryAddressMatchStatus.PENDING_ADDRESS_CONFIRMATION);
        diagnosis.markRegistryProcessed(successful);
        diagnosis.resetRegistryAddressReviewConfirmation();
        return response.toResponse(addressMatchStatus.name());
    }

    @Transactional
    public FastApiDiagnosisResponse analyzeDiagnosis(
            Long userId,
            Long analysisId,
            String accessToken
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, accessToken);
        if (diagnosis.getStatus() != DiagnosisStatus.READY) {
            throw error(
                    HttpStatus.CONFLICT,
                    "DIAGNOSIS_NOT_READY",
                    "진단에 필요한 입력과 서류 확인이 완료되지 않았습니다."
            );
        }
        RegistryResult registryResult = diagnosis.getMode() == DiagnosisMode.DETAILED
                ? latestSuccessfulRegistry(analysisId)
                : null;
        if (registryResult != null) {
            ensureRegistryAddressMatched(
                    registryAddressMatchService.match(
                            diagnosis.getAddress(),
                            diagnosis.getDongName(),
                            diagnosis.getHoName(),
                            readPropertySnapshot(diagnosis.getPropertySnapshot()),
                            registryResult.getRawAddress(),
                            registryResult.getDongName(),
                            registryResult.getHoName()
                    ),
                    diagnosis.isRegistryAddressReviewConfirmed()
            );
        }
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
            Long analysisId,
            String accessToken
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, accessToken);
        DiagnosisResult result = diagnosisResultRepository
                .findByDiagnosisAnalysisId(analysisId)
                .orElseThrow(() -> error(
                        HttpStatus.CONFLICT,
                        "DIAGNOSIS_RESULT_NOT_READY",
                        "완료된 진단 결과가 없습니다."
                ));
        FastApiDiagnosisResponse response = readDiagnosisResponse(result);
        RegistryReportData registry = diagnosis.getMode() == DiagnosisMode.DETAILED
                ? findRegistryReportData(diagnosis)
                : new RegistryReportData(null, null);
        return DiagnosisReportResponse.of(
                response,
                registry.summary(),
                registry.verification()
        );
    }

    public FastApiDiagnosisWhatIfResponse calculateWhatIf(
            Long userId,
            Long analysisId,
            String accessToken,
            DiagnosisWhatIfRequest request
    ) {
        Diagnosis diagnosis = findAccessible(userId, analysisId, accessToken);
        DiagnosisResult result = diagnosisResultRepository
                .findByDiagnosisAnalysisId(analysisId)
                .orElseThrow(() -> error(
                        HttpStatus.CONFLICT,
                        "DIAGNOSIS_RESULT_NOT_READY",
                        "완료된 진단 결과가 없습니다."
                ));
        FastApiDiagnosisResponse response = readDiagnosisResponse(result);
        FastApiDiagnosisWhatIfRequest fastApiRequest =
                FastApiDiagnosisWhatIfRequest.from(
                        diagnosis,
                        response,
                        request
                );
        validateWhatIfRequest(diagnosis, fastApiRequest);
        return fastApiDiagnosisClient.calculateWhatIf(fastApiRequest);
    }

    private void validateWhatIfRequest(
            Diagnosis diagnosis,
            FastApiDiagnosisWhatIfRequest request
    ) {
        if (diagnosis.getMode() == DiagnosisMode.QUICK
                && request.removeActiveMortgage()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "MORTGAGE_SCENARIO_NOT_ALLOWED",
                    "간편진단에는 선순위 근저당 말소 가정을 적용할 수 없습니다."
            );
        }
        if (request.removeActiveMortgage()
                && request.activeMaxClaimAmount() == null) {
            throw error(
                    HttpStatus.CONFLICT,
                    "ACTIVE_MORTGAGE_NOT_CONFIRMED",
                    "말소를 가정할 활성 선순위 근저당을 확인하지 못했습니다."
            );
        }
    }

    private FastApiDiagnosisResponse readDiagnosisResponse(
            DiagnosisResult result
    ) {
        try {
            return JSON_MAPPER.readValue(
                    result.getResponseJson(),
                    FastApiDiagnosisResponse.class
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "저장된 진단 결과를 읽을 수 없습니다.",
                    exception
            );
        }
    }

    private Diagnosis findAccessible(
            Long userId,
            Long analysisId,
            String accessToken
    ) {
        Diagnosis diagnosis = diagnosisRepository.findById(analysisId)
                .orElseThrow(this::diagnosisNotFound);
        if (diagnosis.getUser() != null) {
            if (userId == null
                    || !diagnosis.getUser().getUserId().equals(userId)) {
                throw diagnosisNotFound();
            }
            return diagnosis;
        }
        if (diagnosis.getAnonymousAccessExpiresAt() == null
                || !diagnosis.getAnonymousAccessExpiresAt().isAfter(Instant.now())
                || !accessTokenService.matches(
                        accessToken,
                        diagnosis.getAnonymousAccessTokenHash()
                )) {
            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "ANONYMOUS_DIAGNOSIS_ACCESS_EXPIRED",
                    "익명 진단 접근 정보가 없거나 만료되었습니다."
            );
        }
        return diagnosis;
    }

    private void ensureEditable(Diagnosis diagnosis) {
        if (diagnosis.getStatus() == DiagnosisStatus.ANALYZING
                || diagnosis.getStatus() == DiagnosisStatus.COMPLETED) {
            throw error(
                    HttpStatus.CONFLICT,
                    "DIAGNOSIS_NOT_EDITABLE",
                    "분석을 시작했거나 완료한 진단은 수정할 수 없습니다."
            );
        }
    }

    private void validateDetails(
            Diagnosis diagnosis,
            DiagnosisDetailsRequest request
    ) {
        if (!hasPositiveArea(request.contractArea(), request.exclusiveArea())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "DIAGNOSIS_AREA_REQUIRED",
                    "계약면적 또는 전용면적 중 하나가 필요합니다."
            );
        }
        if (diagnosis.getMode() == DiagnosisMode.DETAILED
                && normalizeOptional(request.landlordName()) == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "LANDLORD_NAME_REQUIRED",
                    "정밀진단에는 계약 상대방 이름이 필요합니다."
            );
        }
    }

    private boolean hasPositiveArea(
            BigDecimal contractArea,
            BigDecimal exclusiveArea
    ) {
        return contractArea != null && contractArea.signum() > 0
                || exclusiveArea != null && exclusiveArea.signum() > 0;
    }

    private void validateRegistryFiles(List<MultipartFile> files) {
        if (files == null || files.size() != 1) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "REGISTRY_FILE_COUNT_INVALID",
                    "등기부등본 PDF 1개만 첨부해 주세요."
            );
        }
        if (files.stream().anyMatch(MultipartFile::isEmpty)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "REGISTRY_FILE_EMPTY",
                    "비어 있는 등기부등본 파일은 첨부할 수 없습니다."
            );
        }
    }

    private RegistryAddressValidation validateRegistryAddressIfPresent(
            Diagnosis diagnosis,
            Long analysisId,
            String address,
            String dongName,
            String hoName,
            Map<String, Object> propertySnapshot,
            boolean reviewConfirmed
    ) {
        if (diagnosis.getMode() != DiagnosisMode.DETAILED) {
            return new RegistryAddressValidation(false, false);
        }
        RegistryResult registryResult = registryResultRepository
                .findTopByAnalysisIdOrderByParsedAtDesc(analysisId)
                .orElse(null);
        if (registryResult == null
                || registryResult.getParseStatus() != ParseStatus.SUCCESS) {
            return new RegistryAddressValidation(false, false);
        }
        RegistryAddressMatchStatus status = registryAddressMatchService.match(
                address,
                dongName,
                hoName,
                propertySnapshot,
                registryResult.getRawAddress(),
                registryResult.getDongName(),
                registryResult.getHoName()
        );
        ensureRegistryAddressMatched(status, reviewConfirmed);
        return new RegistryAddressValidation(
                true,
                status == RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED
        );
    }

    private void ensureRegistryAddressMatched(
            RegistryAddressMatchStatus status,
            boolean reviewConfirmed
    ) {
        if (status == RegistryAddressMatchStatus.MATCH
                || status == RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED
                && reviewConfirmed) {
            return;
        }
        if (status == RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED) {
            throw error(
                    HttpStatus.CONFLICT,
                    "REGISTRY_ADDRESS_PARTIAL_MATCH",
                    "건물 주소는 일치하지만 등기부의 동·호를 확인하지 못했습니다. 입력값과 등기부를 다시 확인해 주세요."
            );
        }
        if (status == RegistryAddressMatchStatus.MISMATCH) {
            throw error(
                    HttpStatus.CONFLICT,
                    "REGISTRY_ADDRESS_MISMATCH",
                    "확정한 주소와 등기부등본의 부동산 주소가 일치하지 않습니다."
            );
        }
        throw error(
                HttpStatus.CONFLICT,
                "REGISTRY_ADDRESS_UNREADABLE",
                "등기부등본에서 비교할 부동산 주소를 읽지 못했습니다."
        );
    }

    private record RegistryAddressValidation(
            boolean registryReady,
            boolean reviewConfirmed
    ) {
    }

    private RegistryResult latestSuccessfulRegistry(Long analysisId) {
        RegistryResult result = registryResultRepository
                .findTopByAnalysisIdOrderByParsedAtDesc(analysisId)
                .orElseThrow(() -> error(
                        HttpStatus.CONFLICT,
                        "REGISTRY_ANALYSIS_REQUIRED",
                        "정밀진단에 사용할 등기부등본 분석이 필요합니다."
                ));
        if (result.getParseStatus() != ParseStatus.SUCCESS) {
            throw error(
                    HttpStatus.CONFLICT,
                    "REGISTRY_ANALYSIS_INCOMPLETE",
                    "등기부등본을 정상적으로 확인하지 못했습니다. 파일을 다시 첨부해 주세요."
            );
        }
        return result;
    }

    private RegistryReportData findRegistryReportData(Diagnosis diagnosis) {
        RegistryResult registryResult = registryResultRepository
                .findTopByAnalysisIdOrderByParsedAtDesc(
                        diagnosis.getAnalysisId()
                )
                .orElse(null);
        if (registryResult == null) {
            return new RegistryReportData(null, null);
        }

        Long registryResultId = registryResult.getRegistryResultId();
        List<RegistryRight> rights = registryRightRepository
                .findByRegistryResultRegistryResultIdOrderByRightIdAsc(
                        registryResultId
                );
        List<RegistryOwner> owners = registryOwnerRepository
                .findByRegistryResultRegistryResultIdOrderByRegistryOwnerIdAsc(
                        registryResultId
                );
        List<LandlordWatchlistCheck> checks = watchlistCheckRepository
                .findByRegistryResultRegistryResultIdOrderByCheckIdAsc(
                        registryResultId
                );
        RegistryAddressMatchStatus addressMatchStatus =
                registryAddressMatchService.match(
                        diagnosis.getAddress(),
                        diagnosis.getDongName(),
                        diagnosis.getHoName(),
                        readPropertySnapshot(diagnosis.getPropertySnapshot()),
                        registryResult.getRawAddress(),
                        registryResult.getDongName(),
                        registryResult.getHoName()
                );
        return new RegistryReportData(
                RegistrySummaryResponse.from(registryResult, rights),
                RegistryVerificationResponse.from(
                        diagnosis,
                        registryResult,
                        rights,
                        owners,
                        checks,
                        addressMatchStatus
                )
        );
    }

    private record RegistryReportData(
            RegistrySummaryResponse summary,
            RegistryVerificationResponse verification
    ) {
    }

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

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private DiagnosisException diagnosisNotFound() {
        return error(
                HttpStatus.NOT_FOUND,
                "DIAGNOSIS_NOT_FOUND",
                "진단 요청을 찾을 수 없습니다."
        );
    }

    private DiagnosisException error(
            HttpStatus status,
            String code,
            String message
    ) {
        return new DiagnosisException(status, code, message);
    }
    public List<DiagnosisListResponse> getCompletedDiagnoses(
            Long userId
    ) {
        List<Diagnosis> diagnoses =
                diagnosisRepository
                        .findAllByUserUserIdOrderByUpdatedAtDesc(
                                userId
                        );

        List<DiagnosisListResponse> responses =
                new ArrayList<>();

        for (Diagnosis diagnosis : diagnoses) {
            DiagnosisListResponse response =
                    DiagnosisListResponse.from(
                            diagnosis
                    );

            responses.add(response);
        }

        return responses;
    }

}
