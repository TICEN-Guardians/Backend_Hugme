package com.project.hugme.infra.ocr.enums;

/** 악성임대인 명단 조회를 실제로 수행했는지. NOT_CHECKED는 등본 파싱 자체가
 *  실패해 소유자를 특정 못 해 조회 자체를 못 한 경우, ERROR는 조회를 시도했으나
 *  DB 장애 등으로 실패한 경우 - 절대 NO_MATCH로 대체하면 안 됨. */
public enum CheckStatus {
    CHECKED, NOT_CHECKED, ERROR
}
