CREATE TABLE agent_recommendations (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    agent_user_id VARCHAR(36) NOT NULL,
    author_user_id VARCHAR(36) NOT NULL,
    rating SMALLINT NOT NULL,
    comment TEXT NOT NULL,
    approval_status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_agent_recommendations_agent FOREIGN KEY (agent_user_id) REFERENCES users (id),
    CONSTRAINT fk_agent_recommendations_author FOREIGN KEY (author_user_id) REFERENCES users (id),
    CONSTRAINT chk_agent_recommendations_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_agent_recommendations_agent_author UNIQUE (agent_user_id, author_user_id)
);

CREATE INDEX idx_agent_recommendations_agent_status
    ON agent_recommendations (agent_user_id, approval_status, created_at DESC);

CREATE INDEX idx_agent_recommendations_author_user_id
    ON agent_recommendations (author_user_id);
