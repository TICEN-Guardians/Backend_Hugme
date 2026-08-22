package com.project.hugme.domain.file.service;

import com.project.hugme.domain.file.dto.FileAccessUrlResponse;
import com.project.hugme.domain.file.dto.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageService {

    private final S3Client uploadS3Client;
    private final S3Presigner uploadS3Presigner;

    @Value("${upload.s3.bucket}")
    private String bucket;

    @Value("${upload.s3.presigned-url-expiration-seconds}")
    private long expirationSeconds;

    public StoredFile store(
            Long applicationId,
            Long documentId,
            MultipartFile file,
            String detectedMimeType
    ) {
        requireBucket();
        String originalName = file.getOriginalFilename() == null
                ? "document"
                : file.getOriginalFilename();
        String extension = extensionFor(detectedMimeType);
        String objectKey = "applications/%d/documents/%d/%s%s".formatted(
                applicationId,
                documentId,
                UUID.randomUUID(),
                extension
        );

        try {
            uploadS3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(detectedMimeType)
                            .contentLength(file.getSize())
                            .serverSideEncryption(ServerSideEncryption.AES256)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException exception) {
            throw new IllegalStateException("S3 파일 업로드에 실패했습니다.", exception);
        }

        return new StoredFile(
                originalName,
                objectKey.substring(objectKey.lastIndexOf('/') + 1),
                objectKey,
                detectedMimeType,
                file.getSize()
        );
    }

    public FileAccessUrlResponse createAccessUrl(
            String objectKey,
            String originalFileName,
            String mimeType,
            boolean inline
    ) {
        requireBucket();
        String disposition = (inline ? "inline" : "attachment")
                + "; filename*=UTF-8''"
                + URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replace("+", "%20");

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .responseContentType(mimeType)
                .responseContentDisposition(disposition)
                .build();

        String url = uploadS3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(expirationSeconds))
                        .getObjectRequest(getRequest)
                        .build()
        ).url().toString();

        return new FileAccessUrlResponse(url, expirationSeconds);
    }

    public void delete(String objectKey) {
        requireBucket();
        uploadS3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }

    private void requireBucket() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("UPLOAD_S3_BUCKET 환경변수가 설정되지 않았습니다.");
        }
    }

    private String extensionFor(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> "";
        };
    }
}
