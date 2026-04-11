CREATE TABLE inquiries (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    sender_user_id VARCHAR(36) NOT NULL,
    recipient_user_id VARCHAR(36) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_inquiries_listing FOREIGN KEY (listing_id) REFERENCES listings (id),
    CONSTRAINT fk_inquiries_sender FOREIGN KEY (sender_user_id) REFERENCES users (id),
    CONSTRAINT fk_inquiries_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (id)
);

CREATE INDEX idx_inquiries_listing_id ON inquiries (listing_id);
CREATE INDEX idx_inquiries_sender_user_id ON inquiries (sender_user_id);
CREATE INDEX idx_inquiries_recipient_user_id ON inquiries (recipient_user_id);
CREATE INDEX idx_inquiries_status ON inquiries (status);
