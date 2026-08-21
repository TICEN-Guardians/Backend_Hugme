-- 비로그인 모의테스트 전용 사용자
INSERT INTO users (
    user_id,
    email,
    password,
    name,
    role,
    status,
    created_at,
    updated_at
)
VALUES (
           0,
           'mock-user@hugme.local',
           NULL,
           '모의테스트 사용자',
           'USER',
           'ACTIVE',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (user_id) DO NOTHING;