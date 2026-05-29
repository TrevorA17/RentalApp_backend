# RentalApp Permission Catalog

This is the authoritative list of permission authorities used in `@PreAuthorize` expressions across the backend, plus the role → permission mapping that drives JWT claims.

**Status: IMPLEMENTED (Phase 6 complete).**

### Implementation simplification

The original plan called for emitting a `permissions: [String]` claim in the JWT. The shipped implementation derives permissions from the `role` claim at authority-assembly time (in `AuthUserPrincipal.getAuthorities()`), since the role → permission mapping is fully static. This keeps tokens smaller and avoids stale-permission risk: every authenticated request gets the *current* permission set for its role, not whatever was true at token-issue time. If/when per-user permission overrides are needed, we'll add the claim then.

---

## Design choices

- **Permissions are an enum, not a DB table.** The role → permission mapping is static (a code constant). This is enough for the MVP and avoids a permissions schema we'd have to seed and maintain. If/when permissions need to be configurable per-tenant at runtime, we can migrate to a DB-backed mapping; until then, the simpler design wins.
- **JWT claim emits a `permissions: [String]` array.** `JwtAuthenticationFilter` builds Spring `SimpleGrantedAuthority` objects from those strings (no `ROLE_` prefix). Controllers use `hasAuthority('PERMISSION_NAME')`.
- **Role authorities (`ROLE_ADMIN`) remain available** for the `hasRole('ADMIN')` checks that already exist — those keep working until each one is migrated. This phase doesn't break them; later cleanup removes redundant role checks.
- **Ownership rules stay in the service layer.** `@PreAuthorize` only enforces "does this user have permission to *attempt* this operation". Ownership (e.g. "you can only edit your own listing") is service-layer responsibility — `SecurityUtils.requireCurrentUserId()` matched against `entity.getOwnerUser().getId()`. That separation matches the existing pattern and keeps `@PreAuthorize` expressions simple.

---

## Catalog (22 permissions)

Naming: `<DOMAIN>_<ACTION>[_OWN]` — uppercase snake-case. `_OWN` suffix marks permissions that only authorize attempting the action; ownership is enforced at the service layer.

### Listings (7)
- `LISTING_VIEW` — view published listings (also available anonymously via permitAll)
- `LISTING_CREATE` — create a draft listing
- `LISTING_UPDATE_OWN` — edit a listing you own
- `LISTING_PUBLISH_OWN` — publish a listing you own
- `LISTING_VIEW_OWN` — list your own listings (incl. drafts)
- `LISTING_UPLOAD_MEDIA` — upload media for attachment to a listing
- `LISTING_MODERATE` — admin queue + approval/rejection

### Saved listings (1)
- `SAVED_LISTING_MANAGE` — save / unsave / list your favorites

### Inquiries (3)
- `INQUIRY_SEND` — send an inquiry to a listing's owner
- `INQUIRY_VIEW_OWN` — list inquiries you sent or received
- `INQUIRY_UPDATE_STATUS` — change status on inquiries you received

### Suggestions (1)
- `SUGGESTION_VIEW` — see personalized listing suggestions

### Profiles (2)
- `PROFILE_VIEW_OWN` — read your own profile
- `PROFILE_UPDATE_OWN` — update your own profile

### Recommendations (2)
- `RECOMMENDATION_SUBMIT` — submit a public testimonial on an agent (not on yourself; admins cannot submit)
- `RECOMMENDATION_MODERATE` — admin approval queue

### Reports (2)
- `REPORT_SUBMIT` — submit a report on a listing or user
- `REPORT_MODERATE` — admin queue + status update

### Admin / Users (2)
- `USER_MODERATE` — list users, change user status (suspend/restore)
- `MODERATION_AUDIT_VIEW` — read the moderation actions audit log

### AI (1)
- `AI_ASSIST_USE` — use AI enhancement endpoints (description-enhance). Note: `/ai/search/interpret` stays `permitAll` since it accepts anonymous queries.

---

## Role → permission mapping

| Permission | RENTER | AGENT | LANDLORD | ADMIN |
|---|:---:|:---:|:---:|:---:|
| `LISTING_VIEW` | ✓ | ✓ | ✓ | ✓ |
| `LISTING_CREATE` | | ✓ | ✓ | |
| `LISTING_UPDATE_OWN` | | ✓ | ✓ | |
| `LISTING_PUBLISH_OWN` | | ✓ | ✓ | |
| `LISTING_VIEW_OWN` | | ✓ | ✓ | |
| `LISTING_UPLOAD_MEDIA` | | ✓ | ✓ | |
| `LISTING_MODERATE` | | | | ✓ |
| `SAVED_LISTING_MANAGE` | ✓ | ✓ | ✓ | |
| `INQUIRY_SEND` | ✓ | ✓ | ✓ | |
| `INQUIRY_VIEW_OWN` | ✓ | ✓ | ✓ | |
| `INQUIRY_UPDATE_STATUS` | | ✓ | ✓ | |
| `SUGGESTION_VIEW` | ✓ | ✓ | ✓ | |
| `PROFILE_VIEW_OWN` | ✓ | ✓ | ✓ | ✓ |
| `PROFILE_UPDATE_OWN` | ✓ | ✓ | ✓ | ✓ |
| `RECOMMENDATION_SUBMIT` | ✓ | ✓ | ✓ | |
| `RECOMMENDATION_MODERATE` | | | | ✓ |
| `REPORT_SUBMIT` | ✓ | ✓ | ✓ | ✓ |
| `REPORT_MODERATE` | | | | ✓ |
| `USER_MODERATE` | | | | ✓ |
| `MODERATION_AUDIT_VIEW` | | | | ✓ |
| `AI_ASSIST_USE` | ✓ | ✓ | ✓ | ✓ |

### Policy notes
- **ADMIN doesn't get `LISTING_CREATE` / `_PUBLISH_OWN` / `_UPDATE_OWN`.** Admins moderate, they don't list properties. (Matches existing rule in `CLAUDE.md`: "Admins cannot submit public recommendations or create listings".)
- **ADMIN doesn't get `SAVED_LISTING_MANAGE` / `INQUIRY_SEND` / `RECOMMENDATION_SUBMIT`.** Admin accounts are operational, not user-flow accounts.
- **ADMIN keeps `REPORT_SUBMIT`.** An admin who notices abuse outside the moderation queue should be able to file a report like anyone else.
- **`INQUIRY_UPDATE_STATUS`** stays AGENT/LANDLORD only — a renter can't mark their own outbound inquiry as "responded". The recipient is always an agent or landlord.

---

## Endpoint → permission mapping

After Phase 6 lands, each `@PreAuthorize` on a controller method uses these:

| Endpoint | Method | Permission |
|---|---|---|
| `/api/v1/auth/**` | various | (none — permitAll or `isAuthenticated()`) |
| `/api/v1/profiles/me` | GET | `PROFILE_VIEW_OWN` |
| `/api/v1/profiles/me` | PUT | `PROFILE_UPDATE_OWN` |
| `/api/v1/profiles/{userId}` | GET | (permitAll — public profile) |
| `/api/v1/listings` | GET | (permitAll — public browse) |
| `/api/v1/listings/{id}` | GET | (permitAll, owner can see draft via service check) |
| `/api/v1/listings` | POST | `LISTING_CREATE` |
| `/api/v1/listings/{id}` | PUT | `LISTING_UPDATE_OWN` (+ service-layer ownership check) |
| `/api/v1/listings/{id}/publish` | POST | `LISTING_PUBLISH_OWN` (+ service-layer ownership check) |
| `/api/v1/listings/media/upload` | POST | `LISTING_UPLOAD_MEDIA` |
| `/api/v1/my/listings` | GET | `LISTING_VIEW_OWN` |
| `/api/v1/amenities` | GET | (permitAll) |
| `/api/v1/saved-listings/**` | * | `SAVED_LISTING_MANAGE` |
| `/api/v1/listings/{id}/save` | POST/DELETE | `SAVED_LISTING_MANAGE` |
| `/api/v1/listings/{id}/inquiries` | POST | `INQUIRY_SEND` |
| `/api/v1/inquiries/sent`, `/received` | GET | `INQUIRY_VIEW_OWN` |
| `/api/v1/inquiries/{id}/status` | PATCH | `INQUIRY_UPDATE_STATUS` (+ service-layer "is recipient" check) |
| `/api/v1/agents/{id}/recommendations` | POST | `RECOMMENDATION_SUBMIT` |
| `/api/v1/agents/{id}/recommendations` | GET | (permitAll — public testimonials) |
| `/api/v1/admin/recommendations` | GET | `RECOMMENDATION_MODERATE` |
| `/api/v1/admin/recommendations/{id}/approval` | PATCH | `RECOMMENDATION_MODERATE` |
| `/api/v1/reports` | POST | `REPORT_SUBMIT` |
| `/api/v1/admin/reports` | GET | `REPORT_MODERATE` |
| `/api/v1/admin/reports/{id}/status` | PATCH | `REPORT_MODERATE` |
| `/api/v1/admin/listings` | GET | `LISTING_MODERATE` |
| `/api/v1/admin/listings/{id}/approval` | PATCH | `LISTING_MODERATE` |
| `/api/v1/admin/users` | GET | `USER_MODERATE` |
| `/api/v1/admin/users/{id}/status` | PATCH | `USER_MODERATE` |
| `/api/v1/admin/moderation-actions` | GET | `MODERATION_AUDIT_VIEW` |
| `/api/v1/ai/listings/description-enhance` | POST | `AI_ASSIST_USE` |
| `/api/v1/ai/search/interpret` | POST | (permitAll) |
| `/api/v1/suggestions/listings` | GET | `SUGGESTION_VIEW` |

---

## Implementation plan (after sign-off)

1. **Add `com.rentalapp.security.Permission` enum** with the 22 values above.
2. **Add static helper `Permission.forRole(Role role): Set<Permission>`** implementing the mapping table. Code lives in `security/` so it has access to `Role`.
3. **`JwtTokenProvider.generateAccessToken`**: include `permissions: [String]` claim (string names of each permission).
4. **`AuthUserPrincipal.getAuthorities()`**: emit one `SimpleGrantedAuthority` per permission AND keep the `ROLE_<role>` authority for backwards compatibility with any remaining `hasRole(...)` checks.
5. **Update every `@PreAuthorize` on controllers** from `hasRole('ADMIN')` to `hasAuthority('<PERMISSION>')` per the endpoint mapping table above.
6. **Drop ROLE_ authority emission** (last — after every controller migrates) — leave for a follow-up if any callers (e.g. SecurityUtils methods, service-layer `requireRole`) still expect role-based.
7. **JWT claim parsing** in `JwtAuthenticationFilter` — read `permissions` claim, build authorities.
8. **Tests**: update `AdminRouteSecurityTest` and any test that builds a principal manually to include the new permissions in its authorities list. Verify a non-admin getting 403 from `hasAuthority('LISTING_MODERATE')` returns the same `FORBIDDEN` code as before (it should — same `RestAccessDeniedHandler` path).
9. **No DB migration needed.** Static enum-based mapping; nothing persists.

### What stays
- `SecurityUtils.requireRole(Role)` — still used by services for ownership checks and admin role verification within service code. Permission authorities don't replace role-based reasoning in services.
- `ROLE_*` authority emission (for now) — removed in a follow-up after all controllers migrate.

### What changes
- Every `@PreAuthorize` on a controller method.
- JWT access token payload (new `permissions` claim).
- `AuthUserPrincipal` authority list (more entries).

### Breaking-change impact
- **Existing access tokens issued before Phase 6 lose `@PreAuthorize` permission checks** on endpoints we migrate. Their `ROLE_*` authorities still work for `hasRole(...)` checks we haven't migrated. Frontends would need users to log in again to get tokens with the new `permissions` claim — but we still emit `ROLE_*` so `hasRole(...)` checks coexist. Net effect: zero downtime; tokens refresh naturally.
- Worst case if a user holds a stale token after Phase 6 lands: any new permission-based endpoint returns 403. They re-login and it works.
