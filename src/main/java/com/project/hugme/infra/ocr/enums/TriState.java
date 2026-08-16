package com.project.hugme.infra.ocr.enums;

/** 압류/가압류/가처분/경매개시/신탁/전세권/임차권 등 권리 플래그용 3상태.
 *  FALSE는 "확인했고 없음", UNKNOWN은 "확인 못 함" - null/false로 뭉개지 않기 위해
 *  boolean 대신 이 enum을 씀. */
public enum TriState {
    TRUE, FALSE, UNKNOWN
}
