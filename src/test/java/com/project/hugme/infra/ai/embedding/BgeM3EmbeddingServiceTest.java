package com.project.hugme.infra.ai.embedding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BgeM3EmbeddingServiceTest {

    @Test
    void embeddingDimensionTest() throws Exception {

        BgeM3ModelLoader modelLoader =
                new BgeM3ModelLoader(
                        "src/main/resources/models/bge-m3/model.onnx"
                );

        BgeM3EmbeddingService embeddingService =
                new BgeM3EmbeddingService(modelLoader);

        float[] embedding =
                embeddingService.embed("전입세대확인서는 어디서 발급받나요?");

        System.out.println("임베딩 차원: " + embedding.length);
        System.out.println("첫 번째 값: " + embedding[0]);

        assertEquals(1024, embedding.length);
    }
}