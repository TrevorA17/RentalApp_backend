CREATE TABLE moderation_actions (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    reason_or_note TEXT,
    actor_user_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_moderation_actions_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

CREATE INDEX idx_moderation_actions_target_created_at
    ON moderation_actions (target_type, target_id, created_at DESC);

CREATE INDEX idx_moderation_actions_actor_user_id
    ON moderation_actions (actor_user_id);
