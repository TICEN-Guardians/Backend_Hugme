ALTER TABLE IF EXISTS registry_results
DROP CONSTRAINT IF EXISTS registry_results_source_type_check;

ALTER TABLE IF EXISTS registry_results
ADD CONSTRAINT registry_results_source_type_check
CHECK (
    source_type IN (
        'PDF_TEXT',
        'PDF_OCR',
        'IMAGE_OCR',
        'IMAGE_OCR_MULTI',
        'PDF_LLM'
    )
);
