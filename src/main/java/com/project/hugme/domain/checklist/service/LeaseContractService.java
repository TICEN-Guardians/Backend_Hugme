package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;

import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LeaseContractService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationInfoRepository applicationInfoRepository;

    public OCRResponse uploadAndAnalyze(
            Long userId,
            Long applicationId,
            MultipartFile file
    ) {
        // 1. 사용자 신청 확인

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


//        // 2. 파일 저장
//        FileUploadResponse uploadedFile =
//                localStorageService.storeFile(file);
////여기서 추가 값 작성해서 ocr테이블 저장
//
////        // 3. FastAPI OCR 호출 및 응답 수신
////        LeaseContractOcrResult ocrResult =
////                leaseContractOcrClient.analyze(
////                        uploadedFile
////                );
//
//        // 4. OCR 결과 DB 저장
//        ApplicationInfo applicationInfo =
//                createApplicationInfo(
//                        application,
//                        ocrResult
//                );
       //

        // 5. 프론트에 반환할 응답 생성
        return null;
    }
}
