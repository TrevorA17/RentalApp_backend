CREATE TABLE reports (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    reporter_user_id VARCHAR(36) NOT NULL,
    listing_id VARCHAR(36),
    reported_user_id VARCHAR(36),
    reason VARCHAR(120) NOT NULL,
    details TEXT,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_reports_reporter_user FOREIGN KEY (reporter_user_id) REFERENCES users (id),
    CONSTRAINT fk_reports_listing FOREIGN KEY (listing_id) REFERENCES listings (id),
    CONSTRAINT fk_reports_reported_user FOREIGN KEY (reported_user_id) REFERENCES users (id)
);

CREATE INDEX idx_reports_status ON reports (status);
CREATE INDEX idx_reports_reporter_user_id ON reports (reporter_user_id);
CREATE INDEX idx_reports_listing_id ON reports (listing_id);
CREATE INDEX idx_reports_reported_user_id ON reports (reported_user_id);
