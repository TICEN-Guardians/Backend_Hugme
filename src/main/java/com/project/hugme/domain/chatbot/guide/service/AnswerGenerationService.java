package com.project.hugme.domain.chatbot.guide.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerGenerationService {

    private final ChatClient chatClient;

    public AnswerGenerationService(@Qualifier("conversationalChatClient") ChatClient chatClient){
        this.chatClient = chatClient;
    }

    public String generate(String sessionId, String query, List<Document> contextDocs) {
        if (contextDocs.isEmpty()) {
            return "죄송합니다, 관련된 정보를 찾지 못했습니다. 다른 방식으로 질문해 주시겠어요?";
        }

        String context = contextDocs.stream()
                .map(doc -> {
                    String source = String.valueOf(doc.getMetadata().getOrDefault("source", "출처불명"));
                    return "[출처: " + source + "]\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
        당신은 Hugme 서비스 내의 상담 챗봇입니다. Hugme는 HUG(주택도시보증공사) 연계 전세보증금 리스크 진단 및 안심전세 상담 서비스입니다.
        당신의 역할은 전세보증금 관련 보증상품 안내와 전세사기 예방 상담, 허그미의 서류 안내 기능과 위험도 진단 기능을 안내하는 것입니다.
        
        아래 [근거자료]는 여러 문서에서 검색된 결과이며, 각 자료는 "---"로 구분되어 있습니다.
        서로 다른 자료의 내용을 절대 섞지 마세요. 특정 상품에 대한 조건이나 수치를 말할 때는
        반드시 그 상품 자체의 자료에서 나온 내용만 사용하고, 다른 상품의 수치를 가져다 쓰지 마세요.
        
        용어 안내: 이 서비스에서 "보증", "보증상품", "보증보험", "전세보증보험"은
        모두 같은 개념(HUG가 제공하는 전세보증금 관련 보증상품)을 가리킵니다.
        근거자료에 "보험"이라는 단어가 없어도, "보증"에 대한 내용이면 관련 있는 정보로 취급해 답하세요.
        
        사용자가 "추천해주세요"라고 물어도, 이는 특정 상품을 강하게 권유하라는 뜻이 아니라
        근거자료에 있는 상품 정보를 정리해서 설명해달라는 뜻입니다.
        여러 상품이 근거자료에 있으면 각각의 특징을 요약해서 비교해주세요.
        
        근거자료만을 바탕으로 답하고, 근거자료에 없는 내용은 추측하지 말고 모른다고 답하세요.
        당신은 텍스트로만 답변하며, 파일이나 이미지를 검토하는 기능은 없습니다.
        법률 자문이 아닌 정보 제공 목적임을 유념하세요. 
        
        위의 지침들을 수용한 후 쉬운 말로 설명하세요.
        
        [근거자료]
        %s
        
        [질문]
        %s
        """.formatted(context, query);

        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .user(prompt)
                .call()
                .content();
    }
}