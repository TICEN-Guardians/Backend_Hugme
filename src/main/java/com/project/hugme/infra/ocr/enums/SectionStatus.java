package com.project.hugme.infra.ocr.enums;

/** 갑구/을구 섹션 자체를 파싱했는지 여부. 을구는 "기록사항 없음"이 명시되는
 *  경우가 있어 CONFIRMED_NONE(실제로 없음 확정)이 별도로 있음 - PARSE_FAILED와
 *  절대 같은 의미로 취급하면 안 됨. */
public enum SectionStatus {
    EXTRACTED, CONFIRMED_NONE, PARSE_FAILED
}
