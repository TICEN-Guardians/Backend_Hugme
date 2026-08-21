package com.project.hugme.domain.file.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hugme.domain.file.entity.DocumentValidationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentGuardrailService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${upload.guardrail.model}")
    private String model;

    public DocumentGuardrailResult validate(
            String expectedDocumentName,
            MultipartFile file,
            String mimeType
    ) {
        String expectedType = expectedType(expectedDocumentName);
        if (expectedType == null) {
            return new DocumentGuardrailResult(
                    DocumentValidationStatus.NOT_SUPPORTED,
                    null,
                    null,
                    "현재 자동 검증을 지원하지 않는 서류입니다."
            );
        }

        try {
            JsonNode response = RestClient.builder()
                    .baseUrl("https://api.openai.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .build()
                    .post()
                    .uri("/v1/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequest(expectedDocumentName, expectedType, file, mimeType))
                    .retrieve()
                    .body(JsonNode.class);

            String outputText = extractOutputText(response);
            JsonNode result = objectMapper.readTree(stripCodeFence(outputText));
            boolean matches = result.path("matchesExpectedType").asBoolean(false);
            double confidence = result.path("confidence").asDouble(0.0);
            String detectedType = result.path("detectedDocumentType").asText("UNKNOWN");
            String message = result.path("message").asText("문서 종류 판별 결과입니다.");

            DocumentValidationStatus status;
            if (confidence < 0.7) {
                status = DocumentValidationStatus.REVIEW_REQUIRED;
            } else {
                status = matches
                        ? DocumentValidationStatus.VALID
                        : DocumentValidationStatus.INVALID;
            }
            return new DocumentGuardrailResult(status, detectedType, confidence, message);
        } catch (Exception exception) {
            return new DocumentGuardrailResult(
                    DocumentValidationStatus.FAILED,
                    null,
                    null,
                    "자동 서류 검증에 실패했습니다."
            );
        }
    }

    private Map<String, Object> createRequest(
            String expectedDocumentName,
            String expectedType,
            MultipartFile file,
            String mimeType
    ) throws IOException {
        String prompt = """
                업로드된 한국어 행정/계약 서류가 기대 서류와 일치하는지 판별하세요.
                기대 서류명: %s
                기대 문서 타입: %s

                판별 대상은 LEASE_CONTRACT(전세계약서), RESIDENT_REGISTRATION(주민등록등본),
                PROPERTY_REGISTRY(부동산 등기사항증명서), UNKNOWN 중 하나입니다.
                신분증만 제출된 경우 주민등록등본으로 판정하지 마세요.
                반드시 설명 없이 아래 JSON 객체만 반환하세요.
                {"matchesExpectedType":true,"detectedDocumentType":"LEASE_CONTRACT","confidence":0.95,"message":"판별 근거를 개인정보 없이 한 문장으로 작성"}
                """.formatted(expectedDocumentName, expectedType);

        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "input_text", "text", prompt));
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        if ("application/pdf".equals(mimeType)) {
            Map<String, Object> fileInput = new LinkedHashMap<>();
            fileInput.put("type", "input_file");
            fileInput.put("filename", safeFileName(file.getOriginalFilename()));
            fileInput.put("file_data", "data:application/pdf;base64," + base64);
            content.add(fileInput);
        } else {
            content.add(Map.of(
                    "type", "input_image",
                    "image_url", "data:" + mimeType + ";base64," + base64
            ));
        }

        return Map.of(
                "model", model,
                "input", List.of(Map.of("role", "user", "content", content))
        );
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("LLM 응답이 없습니다.");
        }
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if (content.hasNonNull("text")) {
                    return content.get("text").asText();
                }
            }
        }
        throw new IllegalStateException("LLM 응답에서 판별 결과를 찾지 못했습니다.");
    }

    private String stripCodeFence(String value) {
        String stripped = value == null ? "" : value.trim();
        if (stripped.startsWith("```")) {
            stripped = stripped.replaceFirst("^```(?:json)?\\s*", "");
            stripped = stripped.replaceFirst("\\s*```$", "");
        }
        return stripped;
    }

    private String expectedType(String documentName) {
        if (documentName == null) {
            return null;
        }
        if (documentName.contains("전세계약서")) {
            return "LEASE_CONTRACT";
        }
        if (documentName.contains("주민등록등본")) {
            return "RESIDENT_REGISTRATION";
        }
        if (documentName.contains("부동산등기부등본") || documentName.contains("등기사항전부증명서")) {
            return "PROPERTY_REGISTRY";
        }
        return null;
    }

    private String safeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "document.pdf";
        }
        return originalFileName.replaceAll("[\\r\\n\\\\/]", "_");
    }
}
