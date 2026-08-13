package com.project.hugme.infra.ai.intent;

import com.project.hugme.infra.ai.intent.DocumentIntentFieldMapper;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentIntentFieldMapperTest {

    @Test
    void singleIntentTest() {

        List<String> fields =
                DocumentIntentFieldMapper.getResponseFields(
                        DocumentQuestionIntent.FEE
                );

        assertThat(fields)
                .containsExactly("fee");
    }

    @Test
    void multipleIntentTest() {

        List<String> fields =
                DocumentIntentFieldMapper.getResponseFields(
                        List.of(
                                DocumentQuestionIntent.ONLINE_ISSUANCE,
                                DocumentQuestionIntent.FEE
                        )
                );

        assertThat(fields)
                .containsExactly(
                        "online_availability",
                        "online_url",
                        "preparation_method",
                        "fee"
                );
    }

    @Test
    void duplicateFieldTest() {

        List<String> fields =
                DocumentIntentFieldMapper.getResponseFields(
                        List.of(
                                DocumentQuestionIntent.ISSUE_METHOD,
                                DocumentQuestionIntent.ONLINE_ISSUANCE
                        )
                );

        assertThat(fields)
                .containsOnlyOnce(
                        "preparation_method",
                        "online_availability",
                        "online_url"
                );
    }

    @Test
    void documentSearchFieldTest() {

        assertThat(
                DocumentIntentFieldMapper.getDocumentSearchFields()
        ).containsExactly(
                "document_name",
                "document_group_name",
                "description",
                "content"
        );

        assertThat(
                DocumentIntentFieldMapper.getEmbeddingField()
        ).isEqualTo("embedding");
    }

    @Test
    void otherIntentTest() {

        List<String> fields =
                DocumentIntentFieldMapper.getResponseFields(
                        DocumentQuestionIntent.OTHER
                );

        assertThat(fields).isEmpty();
    }
}