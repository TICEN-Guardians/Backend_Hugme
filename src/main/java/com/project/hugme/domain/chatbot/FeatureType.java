package com.project.hugme.domain.chatbot;

public enum FeatureType {
    RISK_DIAGNOSIS("위험도 진단", "/imsi_risk", "매물의 전세 사기 위험도를 진단해주는 기능"),
    DOCUMENT_GUIDE("서류 안내", "/imsi-guide", "전세 계약에 필요한 서류를 안내해주는 기능");

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
