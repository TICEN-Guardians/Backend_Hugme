-- V1__create_vector_store_schema.sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_search;

CREATE TABLE IF NOT EXISTS vector_store (
                                            id UUID NOT NULL DEFAULT gen_random_uuid(),
    content TEXT,
    metadata JSON,
    embedding VECTOR(1536),
    CONSTRAINT pk_vector_store PRIMARY KEY (id)
    );

CREATE INDEX IF NOT EXISTS spring_ai_vector_index
    ON vector_store USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS search_idx
    ON vector_store USING bm25 (id, (content::pdb.ngram(2,3)))
    WITH (key_field='id');