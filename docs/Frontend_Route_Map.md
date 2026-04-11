# Frontend Route Map

## Purpose

This document defines the initial route structure for the Next.js frontend based on the BRD and the planned backend API contract.

It is intended to keep the frontend scoped to the MVP and aligned with backend capabilities.

## Frontend Stack Direction

- Next.js
- TypeScript
- MUI
- App Router

## Route Groups

### Public routes

| Route | Purpose |
|---|---|
| `/` | Landing page and featured listing discovery |
| `/listings` | Browse and filter rental listings |
| `/listings/[listingId]` | Listing detail page |
| `/agents/[agentUserId]` | Public agent profile and recommendations |
| `/login` | User login |
| `/register` | Account registration |

### Authenticated renter routes

| Route | Purpose |
|---|---|
| `/dashboard` | Role-aware redirect page |
| `/saved-listings` | Saved/favorited listings |
| `/inquiries/sent` | Renter inquiry history |
| `/profile` | Manage renter profile |

### Authenticated agent or landlord routes

| Route | Purpose |
|---|---|
| `/dashboard` | Poster dashboard summary |
| `/my-listings` | Owned listings |
| `/my-listings/new` | Create listing |
| `/my-listings/[listingId]/edit` | Edit listing |
| `/inquiries/received` | Received inquiries |
| `/profile` | Manage owner profile |

### Admin routes

| Route | Purpose |
|---|---|
| `/admin` | Admin overview |
| `/admin/listings` | Listing moderation queue |
| `/admin/reports` | Report management |
| `/admin/users` | Suspended or flagged user management |

## Initial Screen Priorities

Build screens in this order:

1. `/login`
2. `/register`
3. `/listings`
4. `/listings/[listingId]`
5. `/profile`
6. `/my-listings`
7. `/my-listings/new`
8. `/my-listings/[listingId]/edit`
9. `/saved-listings`
10. `/inquiries/sent`
11. `/inquiries/received`
12. `/admin/listings`
13. `/admin/reports`

## Layout Strategy

### Public layout

Used for:
- landing page
- listing browse
- listing detail
- login/register
- public agent profile

Typical elements:
- top navigation
- search entry point
- footer

### App layout

Used for authenticated renter, agent, and landlord pages.

Typical elements:
- app header
- role-aware sidebar or navigation
- page container

### Admin layout

Used for moderation pages.

Typical elements:
- admin navigation
- moderation tables
- review detail panels

## Route-to-API Mapping

| Frontend Route | Primary API Endpoints |
|---|---|
| `/login` | `POST /api/v1/auth/login` |
| `/register` | `POST /api/v1/auth/register` |
| `/listings` | `GET /api/v1/listings`, `GET /api/v1/amenities` |
| `/listings/[listingId]` | `GET /api/v1/listings/{listingId}`, `POST /api/v1/listings/{listingId}/inquiries`, `POST /api/v1/saved-listings/{listingId}` |
| `/agents/[agentUserId]` | `GET /api/v1/profiles/{userId}`, `GET /api/v1/agents/{agentUserId}/recommendations` |
| `/saved-listings` | `GET /api/v1/saved-listings`, `DELETE /api/v1/saved-listings/{listingId}` |
| `/profile` | `GET /api/v1/profiles/me`, `PUT /api/v1/profiles/me` |
| `/my-listings` | `GET /api/v1/my/listings` |
| `/my-listings/new` | `POST /api/v1/listings`, `GET /api/v1/amenities`, `POST /api/v1/ai/listings/description-enhance` |
| `/my-listings/[listingId]/edit` | `GET /api/v1/listings/{listingId}`, `PUT /api/v1/listings/{listingId}`, `POST /api/v1/media/listings/{listingId}`, `POST /api/v1/listings/{listingId}/publish` |
| `/inquiries/sent` | `GET /api/v1/inquiries/sent` |
| `/inquiries/received` | `GET /api/v1/inquiries/received`, `PATCH /api/v1/inquiries/{inquiryId}/status` |
| `/admin/listings` | `GET /api/v1/admin/listings`, `PATCH /api/v1/admin/listings/{listingId}/approval`, `PATCH /api/v1/admin/listings/{listingId}/disable` |
| `/admin/reports` | `GET /api/v1/admin/reports`, `PATCH /api/v1/admin/reports/{reportId}` |

## Suggested App Directory Structure

```text
src/
  app/
    (public)/
      page.tsx
      listings/
        page.tsx
        [listingId]/
          page.tsx
      agents/
        [agentUserId]/
          page.tsx
      login/
        page.tsx
      register/
        page.tsx
    (app)/
      dashboard/
        page.tsx
      profile/
        page.tsx
      saved-listings/
        page.tsx
      my-listings/
        page.tsx
        new/
          page.tsx
        [listingId]/
          edit/
            page.tsx
      inquiries/
        sent/
          page.tsx
        received/
          page.tsx
    (admin)/
      admin/
        page.tsx
        listings/
          page.tsx
        reports/
          page.tsx
        users/
          page.tsx
  components/
  features/
    auth/
    listings/
    profile/
    inquiries/
    saved-listings/
    admin/
    ai-assist/
  lib/
    api/
    auth/
    validation/
  types/
```

## Shared Frontend Types

Define shared TypeScript types early for:

- `User`
- `Profile`
- `ListingCard`
- `ListingDetail`
- `Amenity`
- `Inquiry`
- `Recommendation`
- `PaginatedResponse<T>`
- enum-like unions for backend enums

## MVP UI Flow Priorities

### Renter flow

1. Register or log in
2. Browse listings
3. Filter by city, area, budget, bedrooms, and house type
4. View listing details
5. Save listing
6. Send inquiry

### Agent or landlord flow

1. Register or log in
2. Complete profile
3. Create draft listing
4. Upload images
5. Use AI description enhancement
6. Publish listing
7. Review received inquiries

### Admin flow

1. Review pending listings
2. Approve, reject, or disable listings
3. Review submitted reports

## State Management Guidance

- Use server state fetching for listing and profile reads.
- Keep auth state centralized.
- Avoid introducing a heavy global state library unless needed after MVP complexity increases.
- Start with route-local form state plus shared API helpers.

## Open Frontend Decisions

- Whether to use MUI theme customization from day one
- Whether image upload gets drag-and-drop in MVP or simple file picker first
- Whether `/dashboard` is role-specific or immediately redirects by role
