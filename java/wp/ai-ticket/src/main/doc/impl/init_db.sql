CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    content TEXT,
    category VARCHAR(50),
    confidence VARCHAR(20),
    status VARCHAR(20),
    trace_id VARCHAR(36), 
    span_id VARCHAR(50), 
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tickets_trace_id ON tickets (trace_id);


CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,           
    username VARCHAR(50) UNIQUE NOT NULL, 
    password VARCHAR(255) NOT NULL,      
    role VARCHAR(20) DEFAULT 'USER',     
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

CREATE INDEX idx_users_username ON users (username);