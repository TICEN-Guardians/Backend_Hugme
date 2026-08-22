package com.project.hugme.domain.file.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Component
public class UploadFileValidator {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final byte[] PDF = {0x25, 0x50, 0x44, 0x46, 0x2D};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public String validateAndDetectMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일은 10MB 이하만 업로드할 수 있습니다.");
        }

        try {
            byte[] header = Arrays.copyOf(file.getBytes(), 8);
            if (startsWith(header, PDF)) {
                return "application/pdf";
            }
            if (startsWith(header, JPEG)) {
                return "image/jpeg";
            }
            if (startsWith(header, PNG)) {
                return "image/png";
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("파일 내용을 확인할 수 없습니다.", exception);
        }

        throw new IllegalArgumentException("PDF, JPEG, PNG 파일만 업로드할 수 있습니다.");
    }

    private boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (source[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }
}
