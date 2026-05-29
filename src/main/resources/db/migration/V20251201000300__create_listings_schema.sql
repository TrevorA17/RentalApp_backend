CREATE TABLE IF NOT EXISTS amenities (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS listings (
    id VARCHAR(36) PRIMARY KEY,
    owner_user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    rent_amount DECIMAL(12,2) NOT NULL,
    deposit_amount DECIMAL(12,2),
    agent_fee_amount DECIMAL(12,2),
    city VARCHAR(120) NOT NULL,
    area VARCHAR(150) NOT NULL,
    bedrooms INTEGER NOT NULL,
    bathrooms INTEGER NOT NULL,
    house_type VARCHAR(30) NOT NULL,
    furnished BOOLEAN NOT NULL,
    availability_status VARCHAR(30) NOT NULL,
    listing_status VARCHAR(30) NOT NULL,
    approval_status VARCHAR(30) NOT NULL,
    owner_type VARCHAR(30) NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_listings_owner_user FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS listing_amenities (
    listing_id VARCHAR(36) NOT NULL,
    amenity_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (listing_id, amenity_id),
    CONSTRAINT fk_listing_amenities_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_listing_amenities_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id)
);

CREATE INDEX IF NOT EXISTS idx_listings_owner_user_id ON listings(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_listings_city ON listings(city);
CREATE INDEX IF NOT EXISTS idx_listings_area ON listings(area);
CREATE INDEX IF NOT EXISTS idx_listings_listing_status ON listings(listing_status);
CREATE INDEX IF NOT EXISTS idx_listings_approval_status ON listings(approval_status);

INSERT INTO amenities (id, name, slug)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Parking', 'parking'),
    ('22222222-2222-2222-2222-222222222222', 'Borehole', 'borehole'),
    ('33333333-3333-3333-3333-333333333333', 'WiFi', 'wifi'),
    ('44444444-4444-4444-4444-444444444444', 'Balcony', 'balcony'),
    ('55555555-5555-5555-5555-555555555555', 'Security', 'security')
ON CONFLICT (id) DO NOTHING;
