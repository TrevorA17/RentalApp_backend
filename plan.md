# RentalApp Implementation Plan

## Purpose

This plan reflects the current state of the MVP after the initial implementation and the terminology correction in Phase 1.

Current product vocabulary:

- `recommendations` = public agent testimonials/reviews
- `suggestions` = personalized listing picks

## Current implementation status

The following vertical slices are implemented:

1. shared foundations
2. auth
3. profiles
4. listings core
5. public listing discovery
6. saved listings
7. inquiries
8. media as URL-based listing media
9. agent recommendations
10. reports and admin moderation
11. AI-assisted listing description enhancement
12. auth hardening and backend test baseline

## What is implemented now

### Shared foundations

- Spring Boot project setup
- Next.js frontend setup
- PostgreSQL local Docker support
- profile-based backend config
- shared response and exception handling

### Auth

- register
- login
- `me`
- refresh token rotation
- logout invalidation

### Profiles

- authenticated profile create/update
- public profile rendering

### Listings

- create
- edit
- publish
- owner listing management
- amenity selection
- media URLs in payload

### Public discovery

- public browse
- filtering by core fields
- listing detail pages

### Saved listings

- save
- unsave
- saved listing retrieval

### Inquiries

- renter inquiry submission
- owner inquiry inbox
- inquiry status updates

### Agent trust

- public agent recommendations/testimonials
- authenticated submission
- backend moderation-compatible approval status

### Suggestions

- personalized listing suggestions for signed-in users
- current data sources:
  - profile
  - saved listings
  - inquiries

### Reports and moderation

- reporting
- listing moderation
- user status moderation
- report status updates

### AI assist

- listing description enhancement
- request logging
- heuristic fallback

## Current gaps after MVP implementation

These are the major gaps that still matter:

### Search quality

- pagination not implemented
- sorting not implemented
- indexing can be improved for heavy listing search use

### Moderation auditability

- no dedicated `moderation_actions` table yet

### Deployment completeness

- Docker support is limited to local Postgres
- no full local stack for backend/frontend/AI services

### AI runtime completeness

- no full Spring AI + Ollama + Qdrant integration yet
- current AI is intentionally light and non-blocking

### Media workflow

- media is URL-based
- no upload endpoint or storage service integration yet

### Admin UI completeness

- backend moderation routes for agent recommendations exist
- no frontend admin page for them yet

## Current recommended implementation order from here

1. documentation sync
2. moderation audit trail
3. search pagination and sorting
4. search-related indexing improvements
5. deployment/docker cleanup
6. AI scope decision:
   - keep current assistive implementation and document phase 2
   - or begin real provider/runtime integration
7. admin recommendation moderation UI
8. media/storage strategy improvement

## Working rules

- keep backend modular by feature
- keep docs aligned with code
- keep AI optional and non-blocking
- avoid reusing `recommendations` to mean personalized listing picks
- preserve MVP simplicity unless a change fixes a real product or maintenance risk
