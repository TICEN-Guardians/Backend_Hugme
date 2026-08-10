package com.project.hugme.domain.chatbot.guide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerGenerationService {

    private final ChatClient chatClient;

    public String generate(String query, List<Document> contextDocs) {
        if (contextDocs.isEmpty()) {
            return "죄송합니다, 관련된 정보를 찾지 못했습니다. 다른 방식으로 질문해 주시겠어요?";
        }

        String context = contextDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                당신은 전세보증금 관련 상담 챗봇입니다.
                아래 [근거자료]는 여러 문서에서 검색된 결과이며, 각 자료는 "---"로 구분되어 있습니다.
                서로 다른 자료의 내용을 절대 섞지 마세요. 특정 상품에 대한 조건이나 수치를 말할 때는
                반드시 그 상품 자체의 자료에서 나온 내용만 사용하고, 다른 상품의 수치를 가져다 쓰지 마세요.
                근거자료만을 바탕으로 답하고, 없는 내용은 추측하지 말고 모른다고 답하세요.
                법률 자문이 아닌 정보 제공 목적임을 유념하고, 쉬운 말로 설명하세요.
                
                [근거자료]
                %s
                
                [질문]
                %s
                """.formatted(context, query);

        return chatClient.prompt().user(prompt).call().content();
    }
}