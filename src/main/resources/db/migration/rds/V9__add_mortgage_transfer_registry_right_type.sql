-- RightType enum에는 MORTGAGE_TRANSFER가 있지만 기존 registry_rights CHECK 제약에는
-- 누락되어 있어 AI가 근저당권이전을 저장할 때 INSERT가 실패한다.
--
-- Flyway가 Hibernate ddl-auto보다 먼저 실행되는 신규 환경에서는 테이블이 아직 없을 수
-- 있으므로 ALTER TABLE IF EXISTS를 사용한다. 기존 환경에서는 제약을 새 목록으로 교체한다.

ALTER TABLE IF EXISTS registry_rights
    DROP CONSTRAINT IF EXISTS registry_rights_right_type_check;

ALTER TABLE IF EXISTS registry_rights
    ADD CONSTRAINT registry_rights_right_type_check
    CHECK (
        right_type IN (
            'OWNERSHIP',
            'MORTGAGE',
            'MORTGAGE_AMEND',
            'MORTGAGE_TRANSFER',
            'SEIZURE',
            'PROVISIONAL_SEIZURE',
            'PROVISIONAL_DISPOSITION',
            'PROVISIONAL_REGISTRATION',
            'AUCTION',
            'TRUST',
            'JEONSE_RIGHT',
            'LEASEHOLD_REGISTRATION',
            'CANCELLATION',
            'OTHER'
        )
    );
