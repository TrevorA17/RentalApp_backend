# API Contract V1

## Purpose

This document describes the current backend API surface for the Rental House Hunting Platform MVP.

Base path:

```text
/api/v1
```

This document reflects the codebase as implemented now, not the earlier design-only version.

## Response conventions

### Success response

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {}
}
```

### Error response

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed.",
  "errors": [
    {
      "field": "email",
      "message": "Email is required."
    }
  ],
  "timestamp": "2026-04-11T12:00:00Z"
}
```

## Authentication

### `POST /auth/register`

Registers a new user.

### `POST /auth/login`

Authenticates a user and returns access and refresh tokens.

### `GET /auth/me`

Returns the authenticated user summary.

Authorization:
- required

### `POST /auth/refresh`

Rotates a refresh token and returns a new token pair.

### `POST /auth/logout`

Invalidates the submitted refresh token.

## Profiles

### `GET /profiles/me`

Returns the authenticated user profile.

Authorization:
- required

### `PUT /profiles/me`

Creates or updates the authenticated user's profile.

### `GET /profiles/{userId}`

Returns public profile information.

## Listings

### `POST /listings`

Creates a draft listing.

Authorization:
- required
- roles: `AGENT`, `LANDLORD`

Request:

```json
{
  "title": "Modern 1 Bedroom Apartment",
  "description": "Spacious apartment in Kilimani",
  "rentAmount": 35000,
  "depositAmount": 35000,
  "agentFeeAmount": 35000,
  "city": "Nairobi",
  "area": "Kilimani",
  "bedrooms": 1,
  "bathrooms": 1,
  "houseType": "APARTMENT",
  "furnished": false,
  "availabilityStatus": "AVAILABLE_NOW",
  "amenityIds": ["uuid-1", "uuid-2"],
  "media": [
    {
      "mediaType": "IMAGE",
      "mediaUrl": "https://example.com/front.jpg",
      "caption": "Front view"
    }
  ]
}
```

Current media behavior:
- media is provided as URL-based items in the listing payload
- there is no standalone upload endpoint in the current implementation

### `PUT /listings/{listingId}`

Updates an existing listing.

### `POST /listings/{listingId}/publish`

Publishes a listing.

### `GET /listings/{listingId}`

Returns listing detail.

### `GET /my/listings`

Returns listings owned by the authenticated user.

### `GET /listings`

Public listing search endpoint.

Current query params:
- `city`
- `area`
- `minPrice`
- `maxPrice`
- `bedrooms`
- `bathrooms`
- `houseType`
- `furnished`
- `amenities`
- `page`
- `size`
- `sort`

Supported `sort` values:
- `PUBLISHED_AT_DESC`
- `RENT_AMOUNT_ASC`
- `RENT_AMOUNT_DESC`
- `CREATED_AT_DESC`

Current response shape inside `data`:

```json
{
  "items": [],
  "page": 0,
  "size": 12,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false,
  "sort": "PUBLISHED_AT_DESC"
}
```

Current visibility rule:
- public browse returns only `PUBLISHED` + `APPROVED` listings

### `GET /amenities`

Returns available amenity options.

## Saved Listings

### `POST /listings/{listingId}/save`

Saves a listing.

### `DELETE /listings/{listingId}/save`

Unsaves a listing.

### `GET /saved-listings`

Returns saved listings for the authenticated user.

### `GET /saved-listings/ids`

Returns saved listing ids for quick UI state checks.

## Inquiries

### `POST /listings/{listingId}/inquiries`

Creates an inquiry for a listing.

### `GET /inquiries/sent`

Returns inquiries sent by the authenticated user.

### `GET /inquiries/received`

Returns inquiries received by the authenticated listing owner.

### `PATCH /inquiries/{inquiryId}/status`

Updates inquiry status.

## Agent Recommendations

In the current product vocabulary, `recommendations` means public agent testimonials or reviews.

### `POST /agents/{agentUserId}/recommendations`

Creates a public recommendation for an agent profile.

Authorization:
- required

Request:

```json
{
  "rating": 5,
  "comment": "Professional, responsive, and transparent throughout the process."
}
```

Current MVP rules:
- target user must be an `AGENT`
- author cannot recommend their own profile
- admin accounts cannot submit recommendations
- one recommendation per author per agent
- newly created recommendations are currently stored as approved in the MVP flow

### `GET /agents/{agentUserId}/recommendations`

Returns public approved recommendations for the agent profile.

## Suggestions

Suggestions are personalized listing picks for signed-in users. They are not agent reviews and should not be called recommendations in product copy.

### `GET /suggestions/listings`

Returns personalized listing suggestions.

Authorization:
- required

Query params:
- `limit`

## Reports

### `POST /reports`

Creates a report on a listing or user.

Authorization:
- required

## Admin Moderation

### `GET /admin/listings`

Returns listings for moderation.

### `PATCH /admin/listings/{listingId}/approval`

Updates listing approval status.

### `GET /admin/reports`

Returns submitted reports.

### `PATCH /admin/reports/{reportId}/status`

Updates report status.

### `GET /admin/users`

Returns users for moderation.

### `PATCH /admin/users/{userId}/status`

Updates user status.

### `GET /admin/recommendations`

Returns agent recommendations for moderation review.

### `PATCH /admin/recommendations/{recommendationId}/approval`

Updates recommendation approval status.

Moderation audit behavior:
- listing, report, and recommendation status-changing admin actions are written to `moderation_actions`

## AI Assist

### `POST /ai/listings/description-enhance`

Generates assistive listing description output.

Current implementation truth:
- lightweight listing enhancement
- heuristic fallback
- request logging in Postgres
- non-blocking behavior

Current non-truth:
- the codebase does not yet provide full Spring AI + Ollama + Qdrant workflow integration

## Current backend module mapping

- `/auth` -> `auth`
- `/profiles` -> `profiles`
- `/listings`, `/amenities` -> `listings`
- `/saved-listings` and listing save endpoints -> `saved`
- `/inquiries` -> `inquiries`
- `/agents/.../recommendations` -> `recommendations`
- `/suggestions/...` -> `suggestions`
- `/reports`, `/admin/...` -> `reports` and `admin`
- listing media DTOs are currently handled through listing payloads
- `/ai` -> `ai`

## Not yet implemented

- standalone file-upload media endpoints
- admin route for browsing moderation history
- full AI provider/vector runtime integration
