package com.project.hugme.infra.elasticsearch.document.service;

import com.project.hugme.infra.elasticsearch.document.dto.DocumentSearchRequest;
import com.project.hugme.infra.elasticsearch.document.dto.DocumentSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocumentSearchFacadeTest {

    @Autowired
    private DocumentSearchFacade documentSearchFacade;

    @Test
    void searchWithoutDocumentIdTest() throws Exception {

        DocumentSearchRequest request =
                new DocumentSearchRequest(
                        null,
                        "등본 발급 수수료 알려줘"
                );

        List<DocumentSearchResponse> results =
                documentSearchFacade.search(request);

        System.out.println("검색 결과 수 = " + results.size());

        for (int i = 0; i < results.size(); i++) {
            DocumentSearchResponse result = results.get(i);

            System.out.println("------------------------");
            System.out.println("rank = " + (i + 1));
            System.out.println("documentId = " + result.documentId());
            System.out.println("documentName = " + result.documentName());
            System.out.println("fields = " + result.fields());
            System.out.println("score = " + result.score());
        }

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(3);
        assertThat(results.get(0).score()).isNotNull();
    }
    @Test
    void searchOtherIntentTest() throws Exception {

        DocumentSearchRequest request =
                new DocumentSearchRequest(
                        null,
                        "이 집 전세사기 위험해?"
                );

        List<DocumentSearchResponse> results =
                documentSearchFacade.search(request);

        System.out.println("검색 결과 수 = " + results.size());

        assertThat(results).isEmpty();
    }
}