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
            "특정 HUG 보증상품의 자격요건을 바탕으로, 그 상품 신청에 필요한 제출 서류 체크리스트를 " +
                    "안내하고 준비 여부를 체크해주는 기능. '이 상품 신청에 어떤 서류가 필요한지' 같은 " +
                    "신청 체크리스트 요청에만 해당하며, 낱개 서류 하나를 어디서·어떻게 발급받는지 묻는 " +
                    "일반적인 질문(예: 등기부등본 발급 방법)이나 서류 내용을 읽고 해석하는 방법을 묻는 " +
                    "지식성 질문은 이 기능이 아니라 prevention으로 분류할 것.");

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
