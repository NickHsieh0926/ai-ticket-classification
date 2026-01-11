CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    content TEXT,
    category VARCHAR(50),
    confidence VARCHAR(20),
    trace_id VARCHAR(36), 
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tickets_trace_id ON tickets (trace_id);