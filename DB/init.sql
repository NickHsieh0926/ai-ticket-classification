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