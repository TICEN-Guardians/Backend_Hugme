package com.project.hugme.domain.file.service;

import com.project.hugme.domain.file.dto.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class LocalStorageService {

    private final String uploadDirectory;

    public LocalStorageService(
            @Value("${upload.local.dir}")
            String uploadDirectory
    ) {
        this.uploadDirectory = uploadDirectory;
    }

    public StoredFile storeFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String storedName =
                generateStoredFileName(originalName);

        String fullPath = getFullPath(storedName);
        File destination = new File(fullPath);

        File parentDirectory = destination.getParentFile();

        if (!parentDirectory.exists()) {
            boolean created = parentDirectory.mkdirs();

            if (!created) {
                throw new RuntimeException(
                        "파일 저장 폴더를 만들 수 없습니다."
                );
            }
        }

        try {
            file.transferTo(destination);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "파일 저장에 실패했습니다.",
                    exception
            );
        }

        return new StoredFile(
                originalName,
                storedName,
                fullPath,
                file.getContentType(),
                file.getSize()
        );
    }

    // 중복되지 않는 저장 파일명 생성
    private String generateStoredFileName(
            String originalName
    ) {
        String extension = "";

        if (originalName != null) {
            int extensionIndex =
                    originalName.lastIndexOf(".");

            if (extensionIndex >= 0) {
                extension =
                        originalName.substring(
                                extensionIndex
                        );
            }
        }

        return UUID.randomUUID() + extension;
    }

    // 저장할 파일의 전체 경로 생성
    private String getFullPath(
            String storedName
    ) {
        return uploadDirectory
                + File.separator
                + storedName;
    }

}
