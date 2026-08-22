DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM diagnoses
        WHERE status NOT IN (
            'CREATED',
            'DETAILS_READY',
            'REGISTRY_ANALYZED',
            'READY',
            'ANALYZING',
            'COMPLETED'
        )
    ) THEN
        RAISE EXCEPTION 'diagnoses.status contains unsupported legacy values';
    END IF;
END
$$;

ALTER TABLE diagnoses
    DROP CONSTRAINT IF EXISTS diagnoses_status_check;

ALTER TABLE diagnoses
    ADD CONSTRAINT diagnoses_status_check
    CHECK (
        status IN (
            'CREATED',
            'DETAILS_READY',
            'REGISTRY_ANALYZED',
            'READY',
            'ANALYZING',
            'COMPLETED'
        )
    );
