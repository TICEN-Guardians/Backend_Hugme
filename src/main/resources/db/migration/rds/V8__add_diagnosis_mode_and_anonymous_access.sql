ALTER TABLE IF EXISTS diagnoses
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE IF EXISTS diagnoses
    ALTER COLUMN address DROP NOT NULL;

ALTER TABLE IF EXISTS diagnoses
    ADD COLUMN IF NOT EXISTS diagnosis_mode VARCHAR(20);

UPDATE diagnoses
SET diagnosis_mode = 'DETAILED'
WHERE diagnosis_mode IS NULL;

ALTER TABLE IF EXISTS diagnoses
    ALTER COLUMN diagnosis_mode SET NOT NULL;

ALTER TABLE IF EXISTS diagnoses
    ADD COLUMN IF NOT EXISTS anonymous_access_token_hash VARCHAR(64);

ALTER TABLE IF EXISTS diagnoses
    ADD COLUMN IF NOT EXISTS anonymous_access_expires_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE IF EXISTS diagnoses
    DROP CONSTRAINT IF EXISTS diagnoses_diagnosis_mode_check;

ALTER TABLE IF EXISTS diagnoses
    ADD CONSTRAINT diagnoses_diagnosis_mode_check
    CHECK (diagnosis_mode IN ('QUICK', 'DETAILED'));
