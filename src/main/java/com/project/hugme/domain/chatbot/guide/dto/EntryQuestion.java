package com.project.hugme.domain.chatbot.guide.dto;

import java.util.List;

public record EntryQuestion(String category, String label, List<String> questions) {

    public static List<EntryQuestion> defaults() {
        return List.of(
                new EntryQuestion(
                        "product",
                        "보증상품 문의",
                        List.of(
                                "전세보증금반환보증이 뭔가요?",
                                "전세금안심대출보증과 전세보증금반환보증은 뭐가 달라요?",
                                "보증 가입 조건이 궁금해요"
                        )
                ),
                new EntryQuestion(
                        "prevention",
                        "전세사기 예방",
                        List.of(
                                "전세사기 예방을 위해 뭘 확인해야 하나요?",
                                "등기부등본은 어떻게 확인하나요?",
                                "계약할 때 조심해야 할 점이 뭔가요?"
                        )
                ),
                new EntryQuestion(
                        "feature",
                        "다른 기능 이용",
                        List.of(
                                "제 매물이 안전한지 확인하고 싶어요",
                                "전세 계약에 필요한 서류가 궁금해요"
                        )
                )
        );
    }
}
