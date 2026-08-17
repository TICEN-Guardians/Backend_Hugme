package com.project.hugme.domain.chatbot;

public enum FeatureType {
    RISK_DIAGNOSIS(
            "위험도 진단",
            "/imsi_risk",
            "사용자가 입력한 특정 매물이나 계약 정보를 바탕으로 전세 사기 위험도를 진단·판정해주는 기능. " +
                    "일반적인 예방 지식이나 확인 방법을 묻는 질문은 이 기능이 아님."),
    DOCUMENT_GUIDE(
            "서류 안내",
            "/imsi-guide",
            "전세 계약·보증 신청 시 제출해야 할 서류 목록과 발급 방법을 안내해주는 기능. " +
                    "등기부등본을 어떻게 읽고 무엇을 확인해야 하는지 같은 지식성 질문은 이 기능이 아니라 일반 상담(prevention)으로 분류할 것.");

    private final String label;
    private final String path;
    private final String description;

    FeatureType(String label, String path, String description) {
        this.label = label;
        this.path = path;
        this.description = description;
    }

    public String label() { return label; }
    public String path() { return path; }
    public String description() { return description; }
}
