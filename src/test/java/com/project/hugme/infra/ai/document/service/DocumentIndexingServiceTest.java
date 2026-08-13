package com.project.hugme.infra.ai.document.service;

import com.project.hugme.infra.ai.document.service.DocumentIndexingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DocumentIndexingServiceTest {

    @Autowired
    private DocumentIndexingService indexingService;

    @Test
    void indexAllTest() throws Exception {
        indexingService.indexAll();
    }
}