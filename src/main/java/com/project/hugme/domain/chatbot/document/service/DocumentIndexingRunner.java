package com.project.hugme.domain.chatbot.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
// @Component
@RequiredArgsConstructor
public class DocumentIndexingRunner implements ApplicationRunner {

    private final DocumentIndexingService documentIndexingService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            documentIndexingService.indexAll();
        } catch (Exception e) {
            log.error("서류 Elasticsearch 자동 색인 실패", e);
        }
    }
}