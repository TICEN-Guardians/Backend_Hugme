package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.client.OcrClient;
import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ContractType;
import com.project.hugme.domain.checklist.entity.application.PartyType;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.HousingTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaseContractService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationInfoRepository applicationinfoRepository;
    private final HousingTypeRepository housingTypeRepository;
    private final OcrClient ocrClient;

    @Transactional
    public OCRResponse uploadAndAnalyze(
            Long userId,
            Long applicationId,
            MultipartFile file
    ) {

        // 1. 신청 정보 확인
        Application application =
                applicationRepository
                        .findByApplicationIdAndUser_UserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "신청정보를 찾을 수 없습니다."
                                )
                        );
        ApplicationInfo applicationInfo =
                applicationinfoRepository
                        .findById(applicationId)
                        .orElseGet(() ->
                                ApplicationInfo.create(application)
                        );

        // 2. FastAPI에 파일 전달 → OCRResponse 바로 수신
        OCRResponse ocrResponse = ocrClient.analyze(file);

        // 3. OCR에서 받은 주택유형 코드로 HousingType 조회
        HousingType housingType =
                housingTypeRepository
                        .findByHousingTypeCode(
                                ocrResponse.housingTypeCode()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "주택유형을 찾을 수 없습니다."
                                )
                        );

        // 4. OCR 결과 DB 반영
        applicationInfo.updateAndConfirm(
                housingType,
                ocrResponse.contractAddress(),
                ocrResponse.contractType(),
                ocrResponse.tenantType(),
                ocrResponse.landlordType(),
                ocrResponse.fixedDateConfirmed(),
                ocrResponse.officetelResidentialMarked(),
                ocrResponse.landlordProxyContract()
        );

        ApplicationInfo savedApplicationInfo =
                applicationinfoRepository.save(applicationInfo);

        // 5. DB에 반영된 Entity 기준으로 응답 생성
        return OCRResponse.from(savedApplicationInfo);
    }
}
