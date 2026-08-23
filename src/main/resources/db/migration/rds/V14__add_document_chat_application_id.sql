DO $$
BEGIN
    IF to_regclass('public.document_chat_histories') IS NOT NULL THEN
        ALTER TABLE document_chat_histories
            ADD COLUMN IF NOT EXISTS application_id BIGINT;

        CREATE INDEX IF NOT EXISTS idx_document_chat_histories_user_application
            ON document_chat_histories (user_id, application_id);
    END IF;
END $$;
