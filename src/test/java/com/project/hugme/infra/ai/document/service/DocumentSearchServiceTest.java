package com.project.hugme.infra.ai.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import com.project.hugme.domain.chatbot.document.service.DocumentSearchService;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
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
    @Test
    void searchByBm25Test() throws Exception {

        List<DocumentSearchResponse> results =
                documentSearchService.searchByBm25(
                        "등본 발급 수수료",
                        List.of(DocumentQuestionIntent.FEE),
                        3
                );

        System.out.println("검색 결과 수 = " + results.size());

        for (DocumentSearchResponse result : results) {
            System.out.println("------------------------");
            System.out.println("documentId = " + result.documentId());
            System.out.println("documentName = " + result.documentName());
            System.out.println("score = " + result.score());
            System.out.println("fields = " + result.fields());
        }

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(3);
    }

    @Test
    void searchByVectorTest() throws Exception {

        List<DocumentSearchResponse> results =
                documentSearchService.searchByVector(
                        "등본 발급 수수료",
                        List.of(DocumentQuestionIntent.FEE),
                        3
                );

        System.out.println("검색 결과 수 = " + results.size());

        for (DocumentSearchResponse result : results) {
            System.out.println("------------------------");
            System.out.println("documentId = " + result.documentId());
            System.out.println("documentName = " + result.documentName());
            System.out.println("score = " + result.score());
            System.out.println("fields = " + result.fields());
        }

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(3);
    }

    @Test
    void searchByHybridTest() throws Exception {

        String query = "등본 발급 수수료";
        List<DocumentQuestionIntent> intents =
                List.of(DocumentQuestionIntent.FEE);
        int topK = 3;

        List<DocumentSearchResponse> bm25Results =
                documentSearchService.searchByBm25(
                        query,
                        intents,
                        topK
                );

        List<DocumentSearchResponse> vectorResults =
                documentSearchService.searchByVector(
                        query,
                        intents,
                        topK
                );

        List<DocumentSearchResponse> hybridResults =
                documentSearchService.searchByHybrid(
                        query,
                        intents,
                        topK
                );

        System.out.println("\n===== BM25 =====");
        printResults(bm25Results);

        System.out.println("\n===== VECTOR =====");
        printResults(vectorResults);

        System.out.println("\n===== HYBRID =====");
        printResults(hybridResults);

        assertThat(hybridResults).isNotEmpty();
        assertThat(hybridResults.size()).isLessThanOrEqualTo(topK);
    }

    private void printResults(
            List<DocumentSearchResponse> results
    ) {

        for (int i = 0; i < results.size(); i++) {

            DocumentSearchResponse result = results.get(i);

            System.out.println("------------------------");
            System.out.println("rank = " + (i + 1));
            System.out.println("documentId = " + result.documentId());
            System.out.println("documentName = " + result.documentName());
            System.out.println("score = " + result.score());
            System.out.println("fields = " + result.fields());
        }
    }

    @Test
    void debugHybridSearchTest() throws Exception {

        String query = "등본 발급 수수료";
        List<DocumentQuestionIntent> intents =
                List.of(DocumentQuestionIntent.FEE);

        int candidateK = 20;
        int topK = 3;

        List<DocumentSearchResponse> bm25Results =
                documentSearchService.searchByBm25(
                        query,
                        intents,
                        candidateK
                );

        List<DocumentSearchResponse> vectorResults =
                documentSearchService.searchByVector(
                        query,
                        intents,
                        candidateK
                );

        List<DocumentSearchResponse> hybridResults =
                documentSearchService.searchByHybrid(
                        query,
                        intents,
                        topK
                );

        System.out.println("\n===== BM25 TOP 20 =====");

        for (int i = 0; i < bm25Results.size(); i++) {
            DocumentSearchResponse result = bm25Results.get(i);

            System.out.println(
                    "rank=" + (i + 1)
                            + " | id=" + result.documentId()
                            + " | name=" + result.documentName()
                            + " | score=" + result.score()
            );
        }

        System.out.println("\n===== VECTOR TOP 20 =====");

        for (int i = 0; i < vectorResults.size(); i++) {
            DocumentSearchResponse result = vectorResults.get(i);

            System.out.println(
                    "rank=" + (i + 1)
                            + " | id=" + result.documentId()
                            + " | name=" + result.documentName()
                            + " | score=" + result.score()
            );
        }

        System.out.println("\n===== HYBRID TOP 3 =====");

        for (int i = 0; i < hybridResults.size(); i++) {

            DocumentSearchResponse result =
                    hybridResults.get(i);

            int bm25Rank =
                    findRank(bm25Results, result.documentId());

            int vectorRank =
                    findRank(vectorResults, result.documentId());

            System.out.println(
                    "rank=" + (i + 1)
                            + " | id=" + result.documentId()
                            + " | name=" + result.documentName()
                            + " | BM25 rank=" + bm25Rank
                            + " | Vector rank=" + vectorRank
                            + " | RRF score=" + result.score()
            );
        }

        assertThat(hybridResults).isNotEmpty();
    }

    private int findRank(
            List<DocumentSearchResponse> results,
            Long documentId
    ) {

        for (int i = 0; i < results.size(); i++) {

            if (results.get(i)
                    .documentId()
                    .equals(documentId)) {

                return i + 1;
            }
        }

        return -1;
    }
}

