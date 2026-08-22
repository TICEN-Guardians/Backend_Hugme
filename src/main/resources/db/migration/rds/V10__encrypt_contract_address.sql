ALTER TABLE application_infos
ALTER COLUMN contract_address TYPE TEXT
    USING contract_address::TEXT;