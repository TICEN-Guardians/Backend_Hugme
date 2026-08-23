DO $$
BEGIN
    IF to_regclass('public.document_chat_histories') IS NOT NULL THEN
        ALTER TABLE document_chat_histories
            ADD COLUMN IF NOT EXISTS session_id VARCHAR(100);

        UPDATE document_chat_histories
        SET session_id = 'legacy-' || history_id
        WHERE session_id IS NULL OR session_id = '';

        ALTER TABLE document_chat_histories
            ALTER COLUMN session_id SET NOT NULL;

        CREATE INDEX IF NOT EXISTS idx_document_chat_histories_user_session_created
            ON document_chat_histories (user_id, session_id, created_at);
    END IF;
END $$;
