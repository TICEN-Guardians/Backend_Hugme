package com.project.hugme.infra.elasticsearch.document;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class DocumentIndexInitializer implements ApplicationRunner{
    private final ElasticsearchClient elasticsearchClient;

    @Value("${hugme.elasticsearch.document-index}")
    private String indexName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index(indexName))
                .value();
        if (exists) {
            return;
        }
        ClassPathResource mappingResource =
                new ClassPathResource("elasticsearch/chatbot-documents-mapping.json");

        try (Reader reader = new InputStreamReader(
                mappingResource.getInputStream(),
                StandardCharsets.UTF_8
        )) {
            elasticsearchClient.indices()
                    .create(c -> c
                            .index(indexName)
                            .withJson(reader));
        }
    }
}
