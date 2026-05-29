CREATE INDEX idx_listings_public_visibility_published_at
    ON listings (listing_status, approval_status, published_at DESC);

CREATE INDEX idx_listings_public_city_area
    ON listings (listing_status, approval_status, city, area);

CREATE INDEX idx_listings_public_rent_amount
    ON listings (listing_status, approval_status, rent_amount);

CREATE INDEX idx_listings_public_house_type
    ON listings (listing_status, approval_status, house_type);

CREATE INDEX idx_listings_public_furnished
    ON listings (listing_status, approval_status, furnished);

CREATE INDEX idx_listings_public_bedrooms
    ON listings (listing_status, approval_status, bedrooms);

CREATE INDEX idx_listings_public_bathrooms
    ON listings (listing_status, approval_status, bathrooms);

CREATE INDEX idx_listings_created_at
    ON listings (created_at DESC);
