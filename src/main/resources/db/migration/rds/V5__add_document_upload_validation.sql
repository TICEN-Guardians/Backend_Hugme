DO $$
BEGIN
    IF to_regclass('public.application_document_uploads') IS NOT NULL THEN
        ALTER TABLE application_document_uploads
            ADD COLUMN IF NOT EXISTS validation_status varchar(30),
            ADD COLUMN IF NOT EXISTS detected_document_type varchar(100),
            ADD COLUMN IF NOT EXISTS validation_confidence double precision,
            ADD COLUMN IF NOT EXISTS validation_message varchar(1000),
            ADD COLUMN IF NOT EXISTS validated_at timestamptz;

        UPDATE application_document_uploads
        SET validation_status = 'NOT_SUPPORTED'
        WHERE validation_status IS NULL;

        ALTER TABLE application_document_uploads
            ALTER COLUMN validation_status SET DEFAULT 'PROCESSING',
            ALTER COLUMN validation_status SET NOT NULL;

        CREATE INDEX IF NOT EXISTS idx_document_upload_application_created
            ON application_document_uploads (application_id, created_at DESC);
    END IF;
END $$;
