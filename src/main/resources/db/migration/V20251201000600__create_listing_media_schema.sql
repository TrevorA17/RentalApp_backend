CREATE TABLE listing_media (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    media_type VARCHAR(30) NOT NULL,
    media_url VARCHAR(1000) NOT NULL,
    caption VARCHAR(255),
    display_order INTEGER NOT NULL,
    CONSTRAINT fk_listing_media_listing FOREIGN KEY (listing_id) REFERENCES listings (id)
);

CREATE INDEX idx_listing_media_listing_id ON listing_media (listing_id);
CREATE INDEX idx_listing_media_display_order ON listing_media (listing_id, display_order);
