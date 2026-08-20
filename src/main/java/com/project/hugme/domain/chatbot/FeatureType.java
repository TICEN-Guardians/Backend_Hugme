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
            "보증 신청을 진행 중인 사용자가 그 신청에 필요한 제출 서류 목록을 확인하거나, " +
                    "서류 준비 여부를 체크하거나, 서류를 어디서·어떻게 발급받는지 절차를 문의하는 기능. " +
                    "상품명이 함께 언급되어도 '서류 목록/준비/발급'이 핵심이면 이 기능이며, " +
                    "서류의 내용을 어떻게 읽고 해석하는지를 묻는 지식성 질문은 이 기능이 아니라 prevention으로 분류할 것.");

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
