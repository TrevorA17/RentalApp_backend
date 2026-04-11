# MVP Data Model

## Purpose

This document describes the current MVP data model for the Rental House Hunting Platform.

It reflects the implemented schema and the current product language:

- `recommendations` = agent testimonials/reviews on public agent profiles
- `suggestions` = personalized listing picks

## Implemented core entities

### `users`

Authenticated accounts for renters, agents, landlords, and admins.

Key fields:
- `id`
- `email`
- `password_hash`
- `full_name`
- `role`
- `status`
- `email_verified`
- `created_at`
- `updated_at`

### `profiles`

Public-facing and editable profile data.

Key fields:
- `user_id`
- `full_name`
- `phone_number`
- `bio`
- `profile_photo_url`
- `city`
- `service_areas`
- `company_name`
- `fee_structure`
- `verification_status`

Current MVP note:
- `service_areas` remains a simple text field

### `amenities`

Reference table for listing amenities.

### `listings`

Rental property listings posted by agents and landlords.

Key fields:
- `owner_user_id`
- `title`
- `description`
- `rent_amount`
- `deposit_amount`
- `agent_fee_amount`
- `city`
- `area`
- `bedrooms`
- `bathrooms`
- `house_type`
- `furnished`
- `availability_status`
- `listing_status`
- `approval_status`
- `owner_type`
- `published_at`
- `archived_at`
- `created_at`
- `updated_at`

### `listing_amenities`

Join table for listing-to-amenity relationships.

### `listing_media`

Ordered media items attached to a listing.

Key fields:
- `listing_id`
- `media_type`
- `media_url`
- `caption`
- `display_order`

Current MVP truth:
- media is URL-based
- the system does not currently upload files directly

### `saved_listings`

Saved or favorited listing relationships.

### `inquiries`

Renter interest and contact flow for a listing.

Key fields:
- `listing_id`
- `sender_user_id`
- `receiver_user_id`
- `message`
- `status`
- `contact_name`
- `contact_phone`
- `created_at`
- `updated_at`

### `reports`

Abuse or trust reports on listings or users.

### `agent_recommendations`

Public recommendations/testimonials left on agent profiles.

Key fields:
- `agent_user_id`
- `author_user_id`
- `rating`
- `comment`
- `approval_status`
- `created_at`
- `updated_at`

Current MVP rules:
- target must be an agent
- one recommendation per author per agent
- author cannot recommend self
- moderation compatibility exists through `approval_status`

### `ai_request_logs`

Audit and observability for AI assist requests.

### `refresh_tokens`

Persisted refresh token lifecycle for auth hardening.

## Current personalized suggestions design

Personalized listing suggestions do not currently have their own dedicated table.

They are generated from:
- profile data
- saved listings
- inquiries
- published listing inventory

This is intentional for MVP simplicity.

## Key relationships

- one `user` has one `profile`
- one `user` can own many `listings`
- one `listing` has many `listing_media`
- one `listing` has many `amenities` through `listing_amenities`
- one `user` can save many listings through `saved_listings`
- one `listing` can receive many `inquiries`
- one `agent` can receive many `agent_recommendations`
- one `user` can author many `agent_recommendations`
- one `listing` or `user` can have many `reports`

## Enums in use

### `Role`

- `RENTER`
- `AGENT`
- `LANDLORD`
- `ADMIN`

### `HouseType`

- `APARTMENT`
- `BEDSITTER`
- `STUDIO`
- `MAISONETTE`
- `BUNGALOW`
- `HOUSE`
- `TOWNHOUSE`

### `AvailabilityStatus`

- `AVAILABLE_NOW`
- `AVAILABLE_SOON`
- `OCCUPIED`

### `ListingStatus`

- `DRAFT`
- `PUBLISHED`
- `UNPUBLISHED`
- `ARCHIVED`
- `DISABLED`

### `ApprovalStatus`

- `PENDING`
- `APPROVED`
- `REJECTED`

Used by:
- listings
- agent recommendations

### `MediaType`

- `IMAGE`
- `VIDEO`

### `InquiryStatus`

- `NEW`
- `CONTACTED`
- `CLOSED`

### `VerificationStatus`

- `UNVERIFIED`
- `PENDING`
- `VERIFIED`

### `ReportStatus`

- `OPEN`
- `RESOLVED`
- `DISMISSED`

## Implemented migration order

1. `V1__create_auth_schema.sql`
2. `V2__create_profiles_schema.sql`
3. `V3__create_listings_schema.sql`
4. `V4__create_saved_listings_schema.sql`
5. `V5__create_inquiries_schema.sql`
6. `V6__create_listing_media_schema.sql`
7. `V7__create_reports_schema.sql`
8. `V8__create_ai_request_logs_schema.sql`
9. `V9__create_refresh_tokens_schema.sql`
10. `V10__create_agent_recommendations_schema.sql`

## Current indexing truth

Already present in schema:
- auth email index
- listing owner index
- listing city index
- listing area index
- listing status index
- listing approval index
- report indexes
- agent recommendation public/admin lookup indexes

Known gap:
- public listing search still needs stronger indexing for price and filter-heavy fields
- pagination and sorting support are not yet implemented

## Not yet implemented

- dedicated moderation action audit table
- dedicated suggestions table
- real file-upload storage flow
- vector database integration
