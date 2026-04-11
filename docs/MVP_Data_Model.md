# MVP Data Model

## Purpose

This document defines the initial domain model for the Rental House Hunting Platform MVP based on the BRD in [Rental_App_Final_Detailed_BRD.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\Rental_App_Final_Detailed_BRD.md).

The goal is to establish a stable backend data foundation before coding application logic or frontend screens.

## Modeling Principles

- Use a modular monolith.
- Keep the schema normalized where it improves integrity.
- Prefer explicit enums for business-state fields.
- Support moderation and auditability from the start.
- Keep AI optional and non-blocking for core workflows.

## Core Entities

### `users`

Represents authenticated platform accounts.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `email` | VARCHAR(255) | Unique, required |
| `password_hash` | VARCHAR(255) | Required |
| `role` | ENUM(Role) | `RENTER`, `AGENT`, `LANDLORD`, `ADMIN` |
| `status` | VARCHAR(50) | Active or suspended state |
| `email_verified` | BOOLEAN | Default `false` |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Rules:
- One account has one primary role in MVP.
- Admin accounts are created internally.

### `profiles`

Represents user-facing profile information.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `user_id` | UUID | FK to `users`, unique |
| `full_name` | VARCHAR(150) | Required |
| `phone_number` | VARCHAR(30) | Nullable |
| `bio` | TEXT | Nullable |
| `profile_photo_url` | TEXT | Nullable |
| `city` | VARCHAR(120) | Nullable |
| `service_areas` | TEXT | Nullable for renter, useful for agent |
| `company_name` | VARCHAR(150) | Optional for agent |
| `fee_structure` | TEXT | Optional for agent |
| `verification_status` | ENUM(VerificationStatus) | Default unverified |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Rules:
- Every user should eventually have one profile.
- `service_areas`, `company_name`, and `fee_structure` are primarily for agents.

### `listings`

Represents rental properties posted by agents or landlords.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `owner_user_id` | UUID | FK to `users` |
| `title` | VARCHAR(200) | Required |
| `description` | TEXT | Required |
| `rent_amount` | DECIMAL(12,2) | Required |
| `deposit_amount` | DECIMAL(12,2) | Nullable |
| `agent_fee_amount` | DECIMAL(12,2) | Nullable |
| `city` | VARCHAR(120) | Required |
| `area` | VARCHAR(150) | Required |
| `bedrooms` | INTEGER | Required |
| `bathrooms` | INTEGER | Required |
| `house_type` | ENUM(HouseType) | Required |
| `furnished` | BOOLEAN | Required |
| `availability_status` | ENUM(AvailabilityStatus) | Required |
| `listing_status` | ENUM(ListingStatus) | Draft, published, etc. |
| `approval_status` | ENUM(ApprovalStatus) | Pending, approved, rejected |
| `owner_type` | ENUM(Role) | Must be `AGENT` or `LANDLORD` |
| `published_at` | TIMESTAMP | Nullable |
| `archived_at` | TIMESTAMP | Nullable |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Rules:
- Only agents and landlords can own listings.
- Public search only returns listings that are both published and approved.
- Agent fee must be clearly stored and exposed when applicable.

### `listing_media`

Stores photos for a listing.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `listing_id` | UUID | FK to `listings` |
| `media_type` | ENUM(MediaType) | MVP can start with `IMAGE` only |
| `url` | TEXT | Required |
| `sort_order` | INTEGER | Default `0` |
| `created_at` | TIMESTAMP | Required |

Rules:
- Validate file type and size before persistence.
- Prefer storing external object-storage URLs rather than binary blobs in Postgres.

### `amenities`

Reference table for available amenities.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `name` | VARCHAR(100) | Unique |
| `slug` | VARCHAR(100) | Unique |

### `listing_amenities`

Join table for listing-to-amenity relationships.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `listing_id` | UUID | FK to `listings` |
| `amenity_id` | UUID | FK to `amenities` |

Composite key:
- `listing_id`
- `amenity_id`

### `saved_listings`

Tracks renter favorites.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `user_id` | UUID | FK to `users` |
| `listing_id` | UUID | FK to `listings` |
| `created_at` | TIMESTAMP | Required |

Constraint:
- Unique on `user_id` + `listing_id`

### `inquiries`

Stores renter interest in a specific listing.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `listing_id` | UUID | FK to `listings` |
| `sender_user_id` | UUID | FK to `users` |
| `receiver_user_id` | UUID | FK to `users` |
| `message` | TEXT | Required |
| `status` | ENUM(InquiryStatus) | `NEW`, `CONTACTED`, `CLOSED` |
| `contact_name` | VARCHAR(150) | Optional fallback |
| `contact_phone` | VARCHAR(30) | Optional fallback |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Rules:
- Sender must be a renter in MVP.
- Inquiry is always tied to one listing.

### `recommendations`

Stores public recommendations on agent profiles.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `agent_user_id` | UUID | FK to `users` |
| `author_user_id` | UUID | FK to `users` |
| `rating` | SMALLINT | Optional, range 1-5 |
| `comment` | TEXT | Required |
| `approval_status` | ENUM(ApprovalStatus) | Moderation support |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Rules:
- MVP allows simple text recommendation with optional rating.
- Recommendations should be hidden until approved if moderation is strict.

### `reports`

Stores abuse or fraud reports.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `reporter_user_id` | UUID | FK to `users` |
| `listing_id` | UUID | Nullable FK to `listings` |
| `reported_user_id` | UUID | Nullable FK to `users` |
| `reason` | VARCHAR(120) | Required |
| `details` | TEXT | Nullable |
| `status` | VARCHAR(50) | Open, reviewed, resolved |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

### `moderation_actions`

Audit trail for admin decisions.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `admin_user_id` | UUID | FK to `users` |
| `target_type` | VARCHAR(50) | `LISTING`, `USER`, `RECOMMENDATION`, `REPORT` |
| `target_id` | UUID | Required |
| `action` | VARCHAR(50) | Approve, reject, disable, suspend |
| `notes` | TEXT | Nullable |
| `created_at` | TIMESTAMP | Required |

### `ai_request_logs`

Stores AI request/response metadata for observability.

Suggested fields:

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key |
| `user_id` | UUID | FK to `users` |
| `listing_id` | UUID | Nullable FK to `listings` |
| `feature_name` | VARCHAR(100) | E.g. `DESCRIPTION_ENHANCER` |
| `input_payload` | TEXT | Redact sensitive data if needed |
| `output_payload` | TEXT | Generated result |
| `status` | VARCHAR(50) | Success or failure |
| `error_message` | TEXT | Nullable |
| `created_at` | TIMESTAMP | Required |

## Enum Definitions

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

### `MediaType`

- `IMAGE`

### `InquiryStatus`

- `NEW`
- `CONTACTED`
- `CLOSED`

### `VerificationStatus`

- `UNVERIFIED`
- `PENDING`
- `VERIFIED`

## Key Relationships

- One `user` has one `profile`.
- One `user` can own many `listings`.
- One `listing` has many `listing_media`.
- One `listing` has many `amenities` through `listing_amenities`.
- One `renter` can save many listings through `saved_listings`.
- One `listing` can receive many `inquiries`.
- One `agent` can receive many `recommendations`.
- One `listing` or `user` can have many `reports`.

## Index Recommendations

Create indexes early for query-heavy fields:

- `users.email`
- `profiles.user_id`
- `listings.owner_user_id`
- `listings.city`
- `listings.area`
- `listings.rent_amount`
- `listings.bedrooms`
- `listings.bathrooms`
- `listings.house_type`
- `listings.availability_status`
- `listings.listing_status`
- `listings.approval_status`
- `saved_listings.user_id`
- `saved_listings.listing_id`
- `inquiries.sender_user_id`
- `inquiries.receiver_user_id`
- `inquiries.listing_id`
- `recommendations.agent_user_id`
- `reports.status`

## Suggested Module Ownership

- `auth`: `users`, auth tokens, password policy
- `profiles`: `profiles`
- `listings`: `listings`, `listing_media`, `amenities`, `listing_amenities`
- `saved`: `saved_listings`
- `inquiries`: `inquiries`
- `recommendations`: `recommendations`
- `admin`: `reports`, `moderation_actions`
- `ai`: `ai_request_logs`

## First Migration Order

1. Enum types
2. `users`
3. `profiles`
4. `amenities`
5. `listings`
6. `listing_media`
7. `listing_amenities`
8. `saved_listings`
9. `inquiries`
10. `recommendations`
11. `reports`
12. `moderation_actions`
13. `ai_request_logs`

## Open Setup Decisions

These still need confirmation before implementation is locked:

- Whether first-time listing publish requires admin approval
- Whether recommendations are auto-visible or moderated first
- Whether `service_areas` should later be normalized into a separate table
- Which media storage provider to use in production
