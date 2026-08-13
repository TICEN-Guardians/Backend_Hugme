package com.project.hugme.infra.ai.document.repository;

import com.project.hugme.infra.ai.document.dto.DocumentSearchData;
import com.project.hugme.infra.ai.document.repository.DocumentSearchDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DocumentSearchDataRepositoryTest {

    @Autowired
    private DocumentSearchDataRepository repository;

    @Test
    void findAllTest() {

        List<DocumentSearchData> documents = repository.findAll();

        System.out.println("조회된 서류 수: " + documents.size());

        documents.stream()
                .limit(3)
                .forEach(System.out::println);
    }
}