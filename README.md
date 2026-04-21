# RentalApp Backend

Spring Boot backend for the Rental House Hunting Platform MVP.

## Current status

This repo is no longer scaffold-only. The backend currently implements:

- JWT auth with access and refresh tokens
- profile management
- rental listing creation, editing, publishing, and public browse
- amenity support
- uploaded listing media stored on the backend filesystem and attached through listing create/update payloads
- saved listings
- renter inquiries
- agent recommendations/testimonials on public agent profiles
- listing suggestions for signed-in users
- reports and admin moderation
- moderation audit trail for admin status changes
- AI-assisted listing description enhancement and natural-language search interpretation with request logging
- Flyway-managed schema migrations
- backend test coverage for core controllers, services, and security

## Stack

- Java 21
- Spring Boot 3.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway

## What is implemented now

### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### Profiles

- `GET /api/v1/profiles/me`
- `PUT /api/v1/profiles/me`
- `GET /api/v1/profiles/{userId}`

### Listings and search

- `GET /api/v1/listings`
- `GET /api/v1/listings/{listingId}`
- `POST /api/v1/listings`
- `POST /api/v1/listings/media/upload`
- `PUT /api/v1/listings/{listingId}`
- `POST /api/v1/listings/{listingId}/publish`
- `GET /api/v1/my/listings`
- `GET /api/v1/amenities`

Public listing browse now supports:

- structured filters
- pagination via `page` and `size`
- sorting via `sort`
- paginated response metadata for the frontend browse experience

### Saved listings

- `POST /api/v1/listings/{listingId}/save`
- `DELETE /api/v1/listings/{listingId}/save`
- `GET /api/v1/saved-listings`
- `GET /api/v1/saved-listings/ids`

### Inquiries

- `POST /api/v1/listings/{listingId}/inquiries`
- `GET /api/v1/inquiries/sent`
- `GET /api/v1/inquiries/received`
- `PATCH /api/v1/inquiries/{inquiryId}/status`

### Agent trust

- `POST /api/v1/agents/{agentUserId}/recommendations`
- `GET /api/v1/agents/{agentUserId}/recommendations`

### Personalized listing suggestions

- `GET /api/v1/suggestions/listings`

### Reports and moderation

- `POST /api/v1/reports`
- `GET /api/v1/admin/listings`
- `PATCH /api/v1/admin/listings/{listingId}/approval`
- `GET /api/v1/admin/reports`
- `PATCH /api/v1/admin/reports/{reportId}/status`
- `GET /api/v1/admin/users`
- `PATCH /api/v1/admin/users/{userId}/status`
- `GET /api/v1/admin/recommendations`
- `PATCH /api/v1/admin/recommendations/{recommendationId}/approval`

### AI assist

- `POST /api/v1/ai/listings/description-enhance`
- `POST /api/v1/ai/search/interpret`

Current AI behavior is intentionally lightweight:

- assistive only
- heuristic fallback implementation
- structured search remains the source of truth
- request logging to Postgres

This repo does not yet contain full Spring AI, Ollama, or Qdrant integration.

## Configuration

The backend uses Spring profiles with `local` as the default.

- `application.yml`: shared config and env-backed production settings
- `application-local.yml`: safe local defaults
- `application-prod.yml`: production baseline

Environment templates:

- [.env.local.example](./.env.local.example)
- [.env.uat.example](./.env.uat.example)

Use them as templates only:

- local backend runs: copy `.env.local.example` to the ignored `.env`
- UAT-style backend runs: copy `.env.uat.example` to the ignored `.env` or inject the same values through your runtime/platform
- do not commit real `.env` files

Required backend env vars outside the local profile defaults:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_MEDIA_STORAGE_PATH`
- `APP_MEDIA_PUBLIC_BASE_URL`
- `JWT_SECRET`

Key local defaults:

- Postgres on `localhost:5433`
- database: `rentalapp`
- username: `postgres`
- password: `postgres`

For containerized runs, the compose backend service uses `SPRING_PROFILES_ACTIVE=prod` and injects the required env vars directly so it talks to the `postgres` service instead of the local-profile `localhost` defaults.

## Local development

### Start the full local app stack

```powershell
docker compose up -d
```

This now starts the currently implemented MVP stack:

- `postgres` on `localhost:5433`
- `backend` on `localhost:8080`
- `frontend` on `localhost:3000`

Uploaded listing media is stored in the compose-managed `listing-media` volume and served publicly from `http://localhost:8080/media/...`.

This compose file does not include Qdrant or Ollama because the current app does not require them.

### Run only Postgres with Docker and the backend locally

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

### Run the backend without Docker

```powershell
.\mvnw.cmd spring-boot:run
```

### Run tests

```powershell
.\mvnw.cmd test
```

## Database migrations

Current migrations:

- `V1__create_auth_schema.sql`
- `V2__create_profiles_schema.sql`
- `V3__create_listings_schema.sql`
- `V4__create_saved_listings_schema.sql`
- `V5__create_inquiries_schema.sql`
- `V6__create_listing_media_schema.sql`
- `V7__create_reports_schema.sql`
- `V8__create_ai_request_logs_schema.sql`
- `V9__create_refresh_tokens_schema.sql`
- `V10__create_agent_recommendations_schema.sql`
- `V11__create_moderation_actions_schema.sql`
- `V12__add_listing_search_indexes.sql`
- `V13__create_password_reset_tokens_schema.sql`

## Known limits

- Qdrant and Ollama are not part of the working runtime stack yet
- AI infra beyond heuristic listing assistance and search interpretation is not wired yet
- Docker is now suitable for local parity and simple container deployment, but it is not a full production platform setup yet
- moderation history UI is intentionally lightweight and recent-actions only
- media uploads are currently image-first and stored on the backend filesystem, not object storage

## Source-of-truth docs

- [Rental_App_Final_Detailed_BRD.md](./Rental_App_Final_Detailed_BRD.md)
- [docs/MVP_Data_Model.md](./docs/MVP_Data_Model.md)
- [docs/API_Contract_V1.md](./docs/API_Contract_V1.md)
- [docs/Frontend_Route_Map.md](./docs/Frontend_Route_Map.md)
- [docs/DEMO_GUIDE.md](./docs/DEMO_GUIDE.md)
- [plan.md](./plan.md)
- [CLAUDE.md](./CLAUDE.md)
