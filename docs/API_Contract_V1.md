# API Contract V1

## Purpose

This document defines the initial REST API surface for the Rental House Hunting Platform MVP. It is intended to align backend implementation and frontend integration before feature coding begins.

Base path:

```text
/api/v1
```

## Response Conventions

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
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

## Authentication

### `POST /auth/register`

Registers a new user.

Request:

```json
{
  "email": "agent@example.com",
  "password": "StrongPassword123!",
  "role": "AGENT",
  "fullName": "Sharon Akinyi"
}
```

Response:

```json
{
  "success": true,
  "message": "Account created",
  "data": {
    "userId": "uuid",
    "role": "AGENT"
  }
}
```

### `POST /auth/login`

Authenticates a user and returns JWT tokens.

Request:

```json
{
  "email": "agent@example.com",
  "password": "StrongPassword123!"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt",
    "refreshToken": "jwt",
    "user": {
      "id": "uuid",
      "email": "agent@example.com",
      "role": "AGENT"
    }
  }
}
```

### `GET /auth/me`

Returns the authenticated user summary.

Authorization:
- Required

### `POST /auth/refresh`

Refreshes the access token.

### `POST /auth/logout`

Invalidates current session tokens if refresh-token tracking is implemented.

## Profiles

### `GET /profiles/me`

Returns the authenticated user's profile.

Authorization:
- Required

### `PUT /profiles/me`

Creates or updates the authenticated user's profile.

Request:

```json
{
  "fullName": "Sharon Akinyi",
  "phoneNumber": "+254700000000",
  "bio": "Independent letting agent",
  "city": "Nairobi",
  "serviceAreas": ["Kilimani", "Kileleshwa"],
  "companyName": "Akinyi Homes",
  "feeStructure": "One month rent",
  "profilePhotoUrl": "https://..."
}
```

### `GET /profiles/{userId}`

Returns public profile information for listing display or agent page rendering.

## Listings

### `POST /listings`

Creates a draft listing.

Authorization:
- Required
- Roles: `AGENT`, `LANDLORD`

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
  "amenityIds": ["uuid-1", "uuid-2"]
}
```

### `GET /listings/{listingId}`

Returns listing detail.

Public endpoint, but only for published and approved listings unless owner or admin is requesting.

### `PUT /listings/{listingId}`

Updates an existing listing.

Authorization:
- Required
- Owner or admin only

### `POST /listings/{listingId}/publish`

Marks a listing ready for publishing.

Authorization:
- Required
- Roles: `AGENT`, `LANDLORD`

Behavior:
- Validates completeness threshold
- Sets moderation state according to chosen approval policy

### `POST /listings/{listingId}/unpublish`

Removes listing from public visibility.

### `POST /listings/{listingId}/archive`

Archives listing.

### `GET /my/listings`

Returns listings owned by the authenticated user.

Authorization:
- Required

Query params:
- `status`
- `approvalStatus`
- `page`
- `size`

## Search

### `GET /listings`

Public listing search endpoint.

Query params:

- `city`
- `area`
- `minPrice`
- `maxPrice`
- `bedrooms`
- `bathrooms`
- `houseType`
- `furnished`
- `availabilityStatus`
- `amenities`
- `sort`
- `page`
- `size`

Response item shape:

```json
{
  "id": "uuid",
  "title": "Modern 1 Bedroom Apartment",
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
  "thumbnailUrl": "https://...",
  "poster": {
    "userId": "uuid",
    "name": "Sharon Akinyi",
    "role": "AGENT",
    "verificationStatus": "VERIFIED"
  }
}
```

## Amenities

### `GET /amenities`

Returns available amenity options for filters and listing forms.

## Saved Listings

### `POST /saved-listings/{listingId}`

Saves a listing for the authenticated renter.

Authorization:
- Required
- Role: `RENTER`

### `DELETE /saved-listings/{listingId}`

Unsaves a listing.

### `GET /saved-listings`

Returns saved listings for the authenticated renter.

## Inquiries

### `POST /listings/{listingId}/inquiries`

Creates an inquiry for a listing.

Authorization:
- Required
- Role: `RENTER`

Request:

```json
{
  "message": "I am interested in viewing this house this weekend.",
  "contactName": "Kevin Otieno",
  "contactPhone": "+254711111111"
}
```

### `GET /inquiries/sent`

Returns inquiries created by the authenticated renter.

Authorization:
- Required

### `GET /inquiries/received`

Returns inquiries received by the authenticated listing owner.

Authorization:
- Required
- Roles: `AGENT`, `LANDLORD`

### `PATCH /inquiries/{inquiryId}/status`

Updates inquiry status.

Request:

```json
{
  "status": "CONTACTED"
}
```

## Recommendations

### `POST /agents/{agentUserId}/recommendations`

Creates a recommendation for an agent profile.

Authorization:
- Required

Request:

```json
{
  "rating": 5,
  "comment": "Professional and transparent throughout the process."
}
```

### `GET /agents/{agentUserId}/recommendations`

Returns visible recommendations for the agent.

## Reports

### `POST /reports`

Creates a content or fraud report.

Authorization:
- Required

Request:

```json
{
  "listingId": "uuid",
  "reportedUserId": "uuid",
  "reason": "SUSPICIOUS_LISTING",
  "details": "The photos appear duplicated from another post."
}
```

## Admin Moderation

### `GET /admin/listings`

Returns listings for moderation review.

Authorization:
- Required
- Role: `ADMIN`

Query params:
- `approvalStatus`
- `listingStatus`
- `page`
- `size`

### `PATCH /admin/listings/{listingId}/approval`

Approves or rejects a listing.

Request:

```json
{
  "approvalStatus": "APPROVED",
  "notes": "Listing meets requirements"
}
```

### `PATCH /admin/listings/{listingId}/disable`

Disables a listing.

### `GET /admin/reports`

Returns submitted reports.

### `PATCH /admin/reports/{reportId}`

Updates report status.

### `PATCH /admin/users/{userId}/suspend`

Suspends a user account.

## Media

### `POST /media/listings/{listingId}`

Uploads listing media.

Authorization:
- Required
- Owner or admin only

MVP note:
- Can start with multipart image upload.
- Production storage should be external to the application container.

### `DELETE /media/{mediaId}`

Deletes listing media owned by the authenticated user or by admin.

## AI Assist

### `POST /ai/listings/description-enhance`

Enhances listing description text.

Authorization:
- Required
- Roles: `AGENT`, `LANDLORD`

Request:

```json
{
  "title": "Modern 1 Bedroom Apartment",
  "description": "1 bedroom to let in Kilimani",
  "city": "Nairobi",
  "area": "Kilimani",
  "bedrooms": 1,
  "bathrooms": 1,
  "houseType": "APARTMENT",
  "furnished": false,
  "amenities": ["Parking", "Borehole"]
}
```

Response:

```json
{
  "success": true,
  "message": "AI suggestion generated",
  "data": {
    "enhancedDescription": "Well-maintained 1-bedroom apartment in Kilimani with parking and reliable water supply.",
    "summaryBullets": [
      "1 bedroom, 1 bathroom",
      "Located in Kilimani",
      "Includes parking and borehole water"
    ],
    "missingInformationSuggestions": [
      "Add monthly deposit amount",
      "Clarify availability date"
    ]
  }
}
```

Rules:
- AI output is advisory only.
- User must explicitly save accepted content.
- AI failure must not block listing creation.

## Initial Security Rules

- JWT bearer authentication for protected endpoints
- Role-based route guards
- Owner checks for user-owned resources
- Input validation on all write endpoints
- File validation on upload endpoints

## Initial Backend Module Mapping

- `/auth` -> `auth`
- `/profiles` -> `profiles`
- `/listings`, `/amenities` -> `listings`
- `/saved-listings` -> `saved`
- `/inquiries` -> `inquiries`
- `/agents/.../recommendations` -> `recommendations`
- `/reports`, `/admin/...` -> `admin`
- `/media` -> `media`
- `/ai` -> `ai`

## Open API Decisions

- Whether refresh tokens are persisted server-side
- Whether admin approval is mandatory before all public listing publishes
- Whether recommendation posting is limited to renters only
- Whether listing creation returns full entity or compact summary
