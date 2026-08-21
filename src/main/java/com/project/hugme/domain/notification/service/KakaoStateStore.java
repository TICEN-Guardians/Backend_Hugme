package com.project.hugme.domain.notification.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KakaoStateStore {

    private static final Duration STATE_VALIDITY =
            Duration.ofMinutes(5);

    private final Map<String, KakaoState> states =
            new ConcurrentHashMap<>();

    /*
     * userId와 applicationId가 연결된
     * 일회용 state를 생성한다.
     */
    public String create(
            Long userId,
            Long applicationId
    ) {
        removeExpiredStates();

        String state =
                UUID.randomUUID().toString();

        KakaoState kakaoState =
                new KakaoState(
                        userId,
                        applicationId,
                        Instant.now()
                                .plus(STATE_VALIDITY)
                );

        states.put(
                state,
                kakaoState
        );

        return state;
    }

    /*
     * 카카오에서 반환된 state를 검증하고,
     * 검증 후 즉시 삭제하여 재사용을 방지한다.
     */
    public void validateAndConsume(
            String state,
            Long userId,
            Long applicationId
    ) {
        KakaoState savedState =
                states.remove(state);

        if (savedState == null) {
            throw new IllegalArgumentException(
                    "유효하지 않은 카카오 인증 요청입니다."
            );
        }

        if (savedState.expiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "카카오 인증 요청이 만료되었습니다."
            );
        }

        if (!savedState.userId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "카카오 인증 사용자 정보가 일치하지 않습니다."
            );
        }

        if (!savedState.applicationId()
                .equals(applicationId)) {

            throw new IllegalArgumentException(
                    "카카오 인증 신청정보가 일치하지 않습니다."
            );
        }
    }

    /*
     * 만료된 state를 메모리에서 제거한다.
     */
    private void removeExpiredStates() {
        Instant now = Instant.now();

        states.entrySet()
                .removeIf(entry ->
                        entry.getValue()
                                .expiresAt()
                                .isBefore(now)
                );
    }

    private record KakaoState(
            Long userId,
            Long applicationId,
            Instant expiresAt
    ) {
    }
}