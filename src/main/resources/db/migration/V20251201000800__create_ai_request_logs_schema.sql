CREATE TABLE ai_request_logs (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    use_case VARCHAR(80) NOT NULL,
    request_payload TEXT NOT NULL,
    response_payload TEXT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    CONSTRAINT fk_ai_request_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_ai_request_logs_user_id ON ai_request_logs (user_id);
CREATE INDEX idx_ai_request_logs_use_case ON ai_request_logs (use_case);
CREATE INDEX idx_ai_request_logs_status ON ai_request_logs (status);
