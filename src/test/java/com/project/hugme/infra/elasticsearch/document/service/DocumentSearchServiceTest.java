package com.project.hugme.infra.elasticsearch.document.service;

import com.project.hugme.infra.elasticsearch.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.elasticsearch.intent.DocumentQuestionIntent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DocumentSearchServiceTest {

    @Autowired
    private DocumentSearchService documentSearchService;

    @Test
    void searchByDocumentIdTest() throws Exception {

        DocumentSearchResponse response =
                documentSearchService.searchByDocumentId(
                        2L,
                        List.of(
                                DocumentQuestionIntent.ONLINE_ISSUANCE,
                                DocumentQuestionIntent.FEE
                        )
                );

        System.out.println("documentId = " + response.documentId());
        System.out.println("documentName = " + response.documentName());
        System.out.println("fields = " + response.fields());
        System.out.println("officialGuideUrl = " + response.officialGuideUrl());
        System.out.println("hugReferenceUrls = " + response.hugReferenceUrls());

        assertThat(response.documentId()).isEqualTo(2L);

        assertThat(response.fields())
                .containsKeys(
                        "online_availability",
                        "online_url",
                        "preparation_method",
                        "fee"
                );
    }
    @Test
    void documentNotFoundTest() {

        assertThatThrownBy(() ->
                documentSearchService.searchByDocumentId(
                        999999L,
                        List.of(DocumentQuestionIntent.FEE)
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 서류를 찾을 수 없습니다");
    }
}

