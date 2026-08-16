package com.project.hugme.domain.checklist.client;


import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OcrClient {

    private final RestClient aiRestClient;

    public OCRResponse analyze(MultipartFile file) {

        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();

            if (file.getContentType() != null) {
                fileHeaders.setContentType(
                        MediaType.parseMediaType(file.getContentType())
                );
            }

            HttpEntity<ByteArrayResource> filePart =
                    new HttpEntity<>(resource, fileHeaders);

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            // FastAPI의
            // files: list[UploadFile] = File(...)
            body.add("files", filePart);

            return aiRestClient.post()
                    .uri("/ocr")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(OCRResponse.class);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "OCR 파일 전송에 실패했습니다.",
                    e
            );
        }
    }
}