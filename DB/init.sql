set timezone to 'ROC';
alter database ai_ticket set timezone to 'ROC';

CREATE TABLE IF NOT EXISTS tickets(
    id BIGSERIAL PRIMARY KEY,
    content TEXT,
    category VARCHAR(50),
    confidence VARCHAR(20),
    status VARCHAR(20),
    trace_id VARCHAR(36), 
    span_id VARCHAR(50),
	reasoning TEXT,
    model VARCHAR(50),
    rag_used BOOLEAN DEFAULT FALSE,
	model_type VARCHAR(10) DEFAULT 'ml',
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tickets_trace_id ON tickets (trace_id);


CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,           
    username VARCHAR(50) UNIQUE NOT NULL, 
    password VARCHAR(255) NOT NULL,      
    role VARCHAR(20) DEFAULT 'USER',     
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);


INSERT INTO users (username, password, role) 
VALUES ('admin', '$2a$10$viOlnU9WQg0QLfGcL2COY.hUQ8fQr5KU1VtIfNrm.lbOURZ7KkXkS', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- pgvector 
CREATE EXTENSION IF NOT EXISTS vector;

-- Embedding 資料表（RAG）
CREATE TABLE IF NOT EXISTS ticket_embeddings (
  id          BIGSERIAL PRIMARY KEY,
  trace_id    VARCHAR(36),
  content     TEXT NOT NULL,
  embedding   vector(384),
  category    VARCHAR(50),
  created_at  TIMESTAMP DEFAULT NOW()
);

-- HNSW 索引（近似最近鄰搜尋）
CREATE INDEX IF NOT EXISTS idx_ticket_embeddings_hnsw
  ON ticket_embeddings
  USING hnsw (embedding vector_cosine_ops);
  
  
CREATE TABLE IF NOT EXISTS semantic_cache (
  id           BIGSERIAL PRIMARY KEY,
  query_text   TEXT NOT NULL,
  embedding    vector(1536),
  result_json  JSONB NOT NULL,
  hit_count    INT DEFAULT 0,
  created_at   TIMESTAMP DEFAULT NOW(),
  last_hit_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_semantic_cache_hnsw
  ON semantic_cache
  USING hnsw (embedding vector_cosine_ops);

  
CREATE OR REPLACE VIEW ab_comparison AS
SELECT
    trace_id,
    content,
    MAX(CASE WHEN model_type = 'ml'  THEN category END) AS ml_category,
    MAX(CASE WHEN model_type = 'llm' THEN category END) AS llm_category,
    MAX(CASE WHEN model_type = 'ml'  THEN confidence::float END) AS ml_confidence,
    MAX(CASE WHEN model_type = 'llm' THEN confidence::float END) AS llm_confidence,
    MAX(CASE WHEN model_type = 'ml' AND category =
        (SELECT MAX(category) FROM tickets t2 WHERE t2.content = tickets.content AND model_type = 'llm')
        THEN 1 ELSE 0 END) AS is_match
FROM tickets
GROUP BY trace_id, content;  