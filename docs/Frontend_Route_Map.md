# Frontend Route Map

## Purpose

This document describes the current Next.js route structure and how it maps to the implemented MVP.

It reflects the current product terminology:

- `recommendations` = public agent testimonials/reviews
- `suggestions` = personalized listing picks shown to signed-in users

## Frontend stack

- Next.js App Router
- TypeScript
- MUI

## Public routes

| Route | Purpose |
|---|---|
| `/` | Landing page and public entry to the rental marketplace |
| `/listings` | Browse and filter published rental listings |
| `/listings/[listingId]` | Public listing detail page |
| `/agents/[agentUserId]` | Public agent profile with recommendations/testimonials |
| `/login` | User login |
| `/register` | Account registration |

## Authenticated app routes

| Route | Purpose |
|---|---|
| `/dashboard` | Role-aware workspace home |
| `/profile` | Manage profile |
| `/saved-listings` | Saved listings |
| `/inquiries/sent` | Sent inquiries |
| `/inquiries/received` | Received inquiries |
| `/my-listings` | Owned listings |
| `/my-listings/new` | Create listing |
| `/my-listings/[listingId]/edit` | Edit listing |

## Admin routes

| Route | Purpose |
|---|---|
| `/admin` | Admin overview |
| `/admin/listings` | Listing moderation |
| `/admin/reports` | Report moderation |
| `/admin/users` | User moderation |

Current limitation:
- there is no dedicated admin page yet for moderating agent recommendations, even though backend endpoints now exist

## Current route-to-API mapping

| Frontend Route | Primary API Endpoints |
|---|---|
| `/login` | `POST /api/v1/auth/login` |
| `/register` | `POST /api/v1/auth/register` |
| `/listings` | `GET /api/v1/listings`, `GET /api/v1/amenities` |
| `/listings/[listingId]` | `GET /api/v1/listings/{listingId}`, `POST /api/v1/listings/{listingId}/inquiries`, `POST /api/v1/listings/{listingId}/save`, `DELETE /api/v1/listings/{listingId}/save`, `POST /api/v1/reports` |
| `/agents/[agentUserId]` | `GET /api/v1/profiles/{userId}`, `GET /api/v1/agents/{agentUserId}/recommendations`, `POST /api/v1/agents/{agentUserId}/recommendations` |
| `/dashboard` | `GET /api/v1/auth/me`, `GET /api/v1/suggestions/listings` |
| `/saved-listings` | `GET /api/v1/saved-listings`, `GET /api/v1/saved-listings/ids`, `DELETE /api/v1/listings/{listingId}/save` |
| `/profile` | `GET /api/v1/profiles/me`, `PUT /api/v1/profiles/me` |
| `/my-listings` | `GET /api/v1/my/listings` |
| `/my-listings/new` | `POST /api/v1/listings`, `GET /api/v1/amenities`, `POST /api/v1/ai/listings/description-enhance` |
| `/my-listings/[listingId]/edit` | `GET /api/v1/listings/{listingId}`, `PUT /api/v1/listings/{listingId}`, `POST /api/v1/listings/{listingId}/publish`, `POST /api/v1/ai/listings/description-enhance` |
| `/inquiries/sent` | `GET /api/v1/inquiries/sent` |
| `/inquiries/received` | `GET /api/v1/inquiries/received`, `PATCH /api/v1/inquiries/{inquiryId}/status` |
| `/admin/listings` | `GET /api/v1/admin/listings`, `PATCH /api/v1/admin/listings/{listingId}/approval` |
| `/admin/reports` | `GET /api/v1/admin/reports`, `PATCH /api/v1/admin/reports/{reportId}/status` |
| `/admin/users` | `GET /api/v1/admin/users`, `PATCH /api/v1/admin/users/{userId}/status` |

## Current UI flow truth

### Renter flow

1. register or log in
2. browse listings with URL-backed filters, sorting, and pagination
3. save listings
4. send inquiries
5. report suspicious listings if needed
6. view suggestions on the dashboard

### Agent flow

1. register or log in
2. complete profile
3. create listing with URL-based media entries
4. use AI description assist
5. publish listing
6. receive inquiries
7. receive public recommendations on the agent profile

### Landlord flow

1. register or log in
2. complete profile
3. create and publish listings
4. receive inquiries

### Admin flow

1. review listings
2. review reports
3. manage user status

Current limitation:
- admin recommendation moderation exists in backend only right now

## Current app directory truth

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
```

## Current implementation notes

- public agent profile page now includes:
  - profile details
  - existing public recommendations
  - authenticated recommendation submission form
- `/listings` keeps filter, page, and sort state in the URL query string
- authenticated feature requests use a centralized refresh-aware API client
- expired access tokens are retried once after refresh before the session is cleared
- listing media is currently managed through URL fields in listing forms
- the frontend does not currently expose a file-upload media flow
- suggestions are shown as a signed-in dashboard feature, not as a public trust feature

## Not yet implemented

- admin recommendation moderation page
- moderation history UI
- file-upload based media workflow
- richer AI workflows beyond description assist
