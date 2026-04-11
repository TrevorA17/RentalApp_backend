# RentalApp Implementation Plan

## Purpose

This plan defines the recommended module-by-module implementation order for the Rental House Hunting Platform MVP.

It is based on:

- [Rental_App_Final_Detailed_BRD.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\Rental_App_Final_Detailed_BRD.md)
- [docs/MVP_Data_Model.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\MVP_Data_Model.md)
- [docs/API_Contract_V1.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\API_Contract_V1.md)
- [docs/Frontend_Route_Map.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\Frontend_Route_Map.md)

The goal is to avoid building disconnected frontend and backend pieces. Each module should move from design to UI to API to integration with clear completion criteria.

## Delivery Strategy

Use a shared-foundation-first, module-by-module approach:

1. Set up shared project foundations once.
2. Build one module at a time across both repos.
3. Start each frontend module with mock data where useful.
4. Replace mocks with real backend integration before marking the module complete.
5. Do not start a later business module before the earlier dependency is stable.

## Working Rules

- Keep the backend as a modular monolith.
- Keep the frontend aligned to the documented API contract.
- Prefer vertical slices over isolated backend-only or frontend-only work.
- Each module must have:
  - data model impact identified
  - backend endpoints defined
  - frontend routes/components defined
  - validation rules defined
  - basic test coverage

## Phase 0: Shared Foundations

This phase happens once before module work begins.

### Backend deliverables

- Initialize Spring Boot project
- Add core dependencies:
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - PostgreSQL Driver
  - Validation
  - Flyway
  - Lombok
- Create base package/module structure:
  - `auth`
  - `profiles`
  - `listings`
  - `saved`
  - `inquiries`
  - `recommendations`
  - `admin`
  - `media`
  - `ai`
- Add environment configuration
- Add Docker Compose for local Postgres
- Add base exception handling and response format

### Frontend deliverables

- Initialize Next.js with TypeScript and App Router
- Add MUI and theme setup
- Create route-group structure from the route map
- Add shared API client layer
- Add shared TypeScript types
- Add app layouts and placeholder screens

### Exit criteria

- Both repos run locally
- Frontend routes render
- Backend starts and connects to Postgres
- Shared response conventions and type direction are documented in code

## Module 1: Auth

This is the first real business module because all protected actions depend on it.

### Scope

- register
- login
- current user lookup
- JWT authentication
- role-aware frontend session handling

### Backend work

- Create `users` table and `Role` enum
- Implement password hashing
- Implement JWT issue and validation
- Build endpoints:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
- Add route protection and role extraction
- Add validation and auth error handling

### Frontend work

- Build `/login`
- Build `/register`
- Add auth form validation
- Add token storage strategy
- Add authenticated route guard
- Add role-aware redirect from `/dashboard`

### Testing

- register success and duplicate-email failure
- login success and invalid-credentials failure
- protected route behavior without token
- protected route behavior with token

### Exit criteria

- Users can register and log in
- Frontend can maintain authenticated session
- Protected routes can detect signed-in user and role

## Module 2: Profiles

Profiles should follow auth immediately because listings and public trust signals depend on them.

### Scope

- user profile creation and update
- public profile display
- agent-specific profile fields

### Backend work

- Create `profiles` table
- Create `VerificationStatus` enum
- Build endpoints:
  - `GET /api/v1/profiles/me`
  - `PUT /api/v1/profiles/me`
  - `GET /api/v1/profiles/{userId}`
- Add role-aware validation for agent-only fields

### Frontend work

- Build `/profile`
- Build public profile rendering for agent pages
- Add profile completion prompts for agents and landlords

### Testing

- create profile
- update profile
- fetch public profile
- invalid field combinations by role

### Exit criteria

- Every authenticated user can manage their profile
- Public listing screens can display poster summary information

## Module 3: Listings Core

This is the core product module. It should be split into authoring and public discovery concerns, but delivered together.

### Scope

- create listing
- edit listing
- draft and publish workflow
- listing detail retrieval

### Backend work

- Create:
  - `listings`
  - `amenities`
  - `listing_amenities`
- Create enums:
  - `HouseType`
  - `AvailabilityStatus`
  - `ListingStatus`
  - `ApprovalStatus`
- Build endpoints:
  - `POST /api/v1/listings`
  - `GET /api/v1/listings/{listingId}`
  - `PUT /api/v1/listings/{listingId}`
  - `POST /api/v1/listings/{listingId}/publish`
  - `POST /api/v1/listings/{listingId}/unpublish`
  - `POST /api/v1/listings/{listingId}/archive`
  - `GET /api/v1/my/listings`
  - `GET /api/v1/amenities`
- Add ownership checks
- Add listing completeness validation

### Frontend work

- Build `/my-listings`
- Build `/my-listings/new`
- Build `/my-listings/[listingId]/edit`
- Add listing form sections:
  - basics
  - pricing
  - location
  - details
  - amenities
  - publish action
- Add draft and validation states

### Testing

- create draft listing
- update listing as owner
- reject unauthorized listing updates
- publish only when completeness threshold is met

### Exit criteria

- Agents and landlords can create and manage listing drafts
- Listing details can be retrieved reliably
- Publish flow works according to the chosen moderation policy

## Module 4: Search and Public Listing Discovery

This makes the product usable for renters.

### Scope

- public listing browse
- public filter/search
- listing cards
- listing detail rendering

### Backend work

- Build `GET /api/v1/listings` with filters
- Add pagination and sorting
- Add indexes for search-heavy columns
- Restrict public results to approved and published listings

### Frontend work

- Build `/`
- Build `/listings`
- Build `/listings/[listingId]`
- Add filters for:
  - city
  - area
  - min/max price
  - bedrooms
  - bathrooms
  - house type
  - amenities
  - furnished
- Add empty, loading, and error states

### Testing

- filter by city
- filter by price range
- filter by bedrooms
- excluded results for unpublished or rejected listings

### Exit criteria

- Renters can browse and filter live listings
- Listing detail page reflects the published public view

## Module 5: Saved Listings

This is a small but high-value renter feature and depends on auth plus public listings.

### Scope

- save listing
- unsave listing
- view saved listings

### Backend work

- Create `saved_listings` table
- Build endpoints:
  - `POST /api/v1/saved-listings/{listingId}`
  - `DELETE /api/v1/saved-listings/{listingId}`
  - `GET /api/v1/saved-listings`

### Frontend work

- Add save button to listing cards and detail page
- Build `/saved-listings`
- Add optimistic or immediate refresh behavior

### Testing

- save listing
- prevent duplicate saves
- unsave listing
- list only the authenticated renter's saved records

### Exit criteria

- Renters can manage favorites cleanly from search and detail screens

## Module 6: Inquiries

This is the first true marketplace conversion module.

### Scope

- renter sends inquiry
- owner receives inquiry
- inquiry status updates

### Backend work

- Create `inquiries` table
- Create `InquiryStatus` enum
- Build endpoints:
  - `POST /api/v1/listings/{listingId}/inquiries`
  - `GET /api/v1/inquiries/sent`
  - `GET /api/v1/inquiries/received`
  - `PATCH /api/v1/inquiries/{inquiryId}/status`
- Add sender/receiver visibility rules

### Frontend work

- Add inquiry form on `/listings/[listingId]`
- Build `/inquiries/sent`
- Build `/inquiries/received`
- Add inquiry status UI for owners

### Testing

- renter submits inquiry
- listing owner sees inquiry
- non-owner cannot view another owner's inquiry inbox
- inquiry status updates correctly

### Exit criteria

- Renters can contact listing owners
- Agents and landlords can track inquiry progress

## Module 7: Media

This module should come after listings core so it plugs into a stable listing workflow.

### Scope

- listing image upload
- image listing and deletion
- file validation

### Backend work

- Create `listing_media` table
- Create `MediaType` enum
- Build endpoints:
  - `POST /api/v1/media/listings/{listingId}`
  - `DELETE /api/v1/media/{mediaId}`
- Implement file validation and storage abstraction

### Frontend work

- Add image upload to listing create/edit flow
- Add gallery management UI
- Add upload states and validation messages

### Testing

- upload valid image
- reject invalid file type
- reject oversized file
- owner-only media modification

### Exit criteria

- Listings can display real image galleries
- Poster can manage listing media safely

## Module 8: Recommendations

This is useful for trust but not as critical as inquiry or discovery.

### Scope

- leave agent recommendation
- show moderated recommendations on public profile

### Backend work

- Create `recommendations` table
- Build endpoints:
  - `POST /api/v1/agents/{agentUserId}/recommendations`
  - `GET /api/v1/agents/{agentUserId}/recommendations`
- Add moderation visibility rules

### Frontend work

- Add recommendation list to `/agents/[agentUserId]`
- Add recommendation submission flow

### Testing

- submit recommendation
- fetch visible recommendations
- hidden recommendations stay hidden before approval if moderation is enabled

### Exit criteria

- Agent trust layer is visible on the public profile

## Module 9: Reports and Admin Moderation

This is essential before public launch, even if some earlier modules are already working.

### Scope

- report listings or users
- review moderation queue
- approve, reject, disable, suspend
- moderation audit trail

### Backend work

- Create:
  - `reports`
  - `moderation_actions`
- Build endpoints:
  - `POST /api/v1/reports`
  - `GET /api/v1/admin/listings`
  - `PATCH /api/v1/admin/listings/{listingId}/approval`
  - `PATCH /api/v1/admin/listings/{listingId}/disable`
  - `GET /api/v1/admin/reports`
  - `PATCH /api/v1/admin/reports/{reportId}`
  - `PATCH /api/v1/admin/users/{userId}/suspend`
- Enforce admin authorization

### Frontend work

- Add report action on listing detail
- Build `/admin`
- Build `/admin/listings`
- Build `/admin/reports`
- Build `/admin/users`

### Testing

- report creation
- admin-only moderation access
- listing approval/rejection flow
- disabled listings removed from public search
- suspended users blocked appropriately

### Exit criteria

- Platform trust controls are operational
- Admin can manage listing and abuse workflows

## Module 10: AI Assist

This module is intentionally last in the build order for MVP implementation, even though it is important strategically.

### Why last

- The platform must remain usable without AI.
- AI should enhance listing creation, not define the core business flow.
- It depends on stable listing forms and persistence.

### Scope

- description enhancement
- summary bullet generation
- missing information suggestions
- request logging

### Backend work

- Create `ai_request_logs`
- Integrate Spring AI
- Implement `POST /api/v1/ai/listings/description-enhance`
- Add graceful fallback when AI service is unavailable

### Frontend work

- Add AI assist panel to listing create/edit screens
- Let users accept, edit, or ignore suggestions
- Clearly label AI-generated content

### Testing

- AI suggestion request success
- AI outage fallback behavior
- accepted AI text remains editable before save

### Exit criteria

- AI can improve listing quality without blocking the listing workflow

## Cross-Cutting Tasks

These should happen progressively, not left until the end.

### Testing

- backend unit tests
- backend integration tests
- frontend component tests where useful
- frontend flow tests for critical paths

### Documentation

- keep API contract updated when endpoints change
- keep route map updated when routes change
- add setup instructions as repos become runnable

### DevOps

- Docker Compose for local services
- environment variable documentation
- production configuration placeholders

### Security

- password hashing
- JWT handling
- role checks
- ownership checks
- upload validation

## Recommended Build Order Summary

1. Phase 0: Shared foundations
2. Module 1: Auth
3. Module 2: Profiles
4. Module 3: Listings core
5. Module 4: Search and public discovery
6. Module 5: Saved listings
7. Module 6: Inquiries
8. Module 7: Media
9. Module 8: Recommendations
10. Module 9: Reports and admin moderation
11. Module 10: AI assist

## What To Start Next

The next implementation step should be:

1. scaffold the frontend foundation in `RentalApp_Frontend`
2. scaffold the backend foundation in `RentalApp_backend`
3. implement Module 1: Auth across both repos

That sequence gives you a clean vertical slice and a real working baseline for all later modules.
