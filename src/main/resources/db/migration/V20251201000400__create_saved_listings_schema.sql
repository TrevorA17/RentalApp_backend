CREATE TABLE saved_listings (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_saved_listings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_saved_listings_listing FOREIGN KEY (listing_id) REFERENCES listings (id),
    CONSTRAINT uk_saved_listings_user_listing UNIQUE (user_id, listing_id)
);

CREATE INDEX idx_saved_listings_user_id ON saved_listings (user_id);
CREATE INDEX idx_saved_listings_listing_id ON saved_listings (listing_id);
