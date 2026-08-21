package com.project.hugme.domain.file.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadFileValidatorTest {

    private final UploadFileValidator validator = new UploadFileValidator();

    @Test
    void detectsPdfFromContentInsteadOfDeclaredMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.bin",
                "application/octet-stream",
                "%PDF-1.7 sample".getBytes()
        );

        assertEquals("application/pdf", validator.validateAndDetectMimeType(file));
    }

    @Test
    void rejectsUnsupportedContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "plain text".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateAndDetectMimeType(file)
        );
    }
}
