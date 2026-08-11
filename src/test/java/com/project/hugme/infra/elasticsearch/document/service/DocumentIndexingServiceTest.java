package com.project.hugme.infra.elasticsearch.document.service;

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