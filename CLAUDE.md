# Claude Code Instructions for RentalApp

Rental house hunting platform focused on rentals only. This is the **single source of truth** for both backend and frontend repos.

- **Backend repo:** `RentalApp_backend/`
- **Frontend repo:** `RentalApp_Frontend/` (sibling directory)

Primary project docs:

- [Rental_App_Final_Detailed_BRD.md](./Rental_App_Final_Detailed_BRD.md)
- [docs/MVP_Data_Model.md](./docs/MVP_Data_Model.md)
- [docs/API_Contract_V1.md](./docs/API_Contract_V1.md)
- [docs/Frontend_Route_Map.md](./docs/Frontend_Route_Map.md)
- [plan.md](./plan.md)

---

## Migration status (in progress)

The conventions in this file describe the **target** state. The codebase is being migrated to match. Status by phase:

- [x] **Phase 0** — This document + `CLAUDE_REVIEW.md`
- [x] **Phase 1** — Cheap convention sweep (`@PreAuthorize` on admin endpoints, unified `FORBIDDEN` code, catch-all error logging)
- [x] **Phase 2** — Two-layer repository pattern (strict ISP) + `I<Service>`/`<Service>` rename
- [x] **Phase 3** — `BaseEntity` upgrade (UUID v7, audit fields, version, soft-delete fields) + JPA auditing wiring
- [x] **Phase 4** — Soft-delete enforcement via `@SQLRestriction` + soft-delete policy
- [x] **Phase 5** — Pagination contract switched to 1-indexed `page` / `perPage` (breaking API change — frontend coordination required)
- [x] **Phase 6** — Permission-based authorities via `@PreAuthorize("hasAuthority('...')")`. See `docs/Permission_Catalog.md` for the 22-permission catalog and role mapping.
- [x] **Phase 7** — Flyway migrations renumbered to `V{yyyyMMddHHmmss}__description.sql`. Helper script: `./scripts/new-migration.sh "description"`.

The migration is complete. All code now reflects the target conventions. The remaining backlog task (request DTO `@Data` standardization) is style-only and queued to fold into a future PR that touches DTOs.

---

## Product boundaries

The MVP is for:

- renter registration and login
- renter, agent, landlord, and admin roles
- profile management
- rental listing creation and management
- public listing browse and filtering
- listing detail pages
- saved listings
- inquiry workflows
- public agent recommendations/testimonials
- personalized listing suggestions
- reporting and moderation
- AI-assisted listing enhancement

The MVP is not for:

- property sales
- payments
- tenancy workflows
- live chat
- full map search
- advanced fraud scoring
- full AI infrastructure claims that are not yet implemented

## Product terminology rules

These names must stay consistent:

- `recommendations` = public agent testimonials/reviews on agent profiles
- `suggestions` = personalized listing picks for signed-in users

Do not reintroduce ambiguous naming.

---

## Error Handling Policy

- **All user-facing error messages originate from the backend** — the frontend must NEVER hardcode error strings.
- Backend returns errors via `ApiResponse` with `code` and `message` fields, mapped centrally by `GlobalExceptionHandler`.
- Frontend extracts and displays the backend message via a shared `extractApiError(err)` helper — never invent error text on the client side.
- Validation errors (field-level) come from Jakarta Bean Validation on backend DTOs and are surfaced as-is.

---

## Backend

### Technology Stack

- Java 21
- Spring Boot 3.4
- Spring Security
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Lombok, Jakarta Bean Validation
- JWT (jjwt 0.12.6), BCrypt
- UUID v7 IDs (post Phase 3), soft deletes (post Phase 4), optimistic locking (post Phase 3)

### Architecture & SOLID Principles

#### Package Structure

- **Package by feature** — each feature lives under `module/<feature>/` containing entity, repository (custom interface + Spring Data impl), service interface, service implementation, controller, and DTOs.
- **Infrastructure** lives in `config/`, `security/`, `exception/`, `common/`.
- Spring profiles: `local` (default), `prod`. `test` is implicit for the test suite.

#### Single Responsibility (SRP)

- **Controllers** are thin — routing + `@PreAuthorize` authorization + delegation only. Zero business logic.
- **Services** own business logic and transactions. One service per feature domain.
- **Repositories** handle data access only. No business rules in queries.
- **DTOs** are data carriers only — no behavior, no business logic.
- **Entities** represent domain state + JPA mappings. Convenience getters are acceptable; no business logic.

#### Open/Closed (OCP)

- Extend behavior through new service methods and new DTOs — don't modify existing method signatures that other code depends on.
- Add new enum values via Flyway migrations — never rename or remove existing enum values without a migration.
- New features get new modules under `module/` — don't bloat existing modules.

#### Liskov Substitution (LSP)

- All entities extend `BaseEntity` and honor its contract (soft deletes, auditing, UUID v7 IDs, optimistic locking).
- All custom exceptions extend `ApiException` — the global handler depends on this hierarchy.
- All services implement their `I<Service>` interface — any implementation must fulfill the full contract.

#### Interface Segregation (ISP)

- **Service interfaces** (`IAuthService`, `IListingService`) define focused contracts per feature — never a god-interface.
- **Repository interfaces** (`IUserRepository`) define the custom query contract separately from Spring's `JpaRepository`.
- Clients depend on the interface, not the implementation.

#### Dependency Inversion (DIP)

- **Constructor injection only** — never `@Autowired` field or setter injection.
- Services depend on interfaces (`IUserRepository`, `IListingRepository`), not concrete classes.
- Configuration and infrastructure wired via Spring — no `new` for managed beans.

### Current backend modules

- `auth`, `profiles`, `listings`, `saved`, `inquiries`, `recommendations`, `suggestions`, `admin`, `reports`, `media`, `ai`

### Entities

#### BaseEntity contract (target — Phase 3)

All entities extend `BaseEntity` which provides:

- `id` (String, UUID v7 via `IdGenerator.newId()`) — auto-generated `@PrePersist`
- `isDeleted`, `deletedAt`, `deletedBy` — soft delete support
- `createdBy`, `updatedBy` — JPA auditing from `SecurityContext`
- `version` — optimistic locking (`@Version`)
- `createdAt`, `updatedAt` — auto timestamps

#### Entity conventions

```java
@Entity
@Table(name = "listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Listing extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)       // ALWAYS LAZY — never EAGER
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)             // String-backed enums
    @Column(nullable = false, length = 30)
    private ListingStatus status = ListingStatus.DRAFT;
}
```

- **LAZY fetch everywhere** — no EAGER. Use `@EntityGraph` or JOIN queries when related data is needed.
- Tables and columns are **snake_case** (Spring/Postgres default). DB identifiers are unquoted.
- Enums are stored as `VARCHAR` via `@Enumerated(EnumType.STRING)` — not Postgres native enum types. (Deliberate divergence from Swirra; simpler for this MVP.)
- Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` on entities.

#### Naming conventions

| Identifier | Convention | Examples |
|---|---|---|
| Java fields, parameters, locals | `camelCase` | `listingId`, `createdAt`, `isPublished` |
| Java classes, enums, records | `PascalCase` | `ListingService`, `ListingStatus`, `MediaUploadResponse` |
| Java packages | `lowercase.singleword` | `com.rentalapp.module.listings` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_PAGE_SIZE`, `DEFAULT_SORT` |
| DB tables | `snake_case` | `users`, `listings`, `listing_media` |
| DB columns | `snake_case` | `created_at`, `owner_id`, `is_deleted` |
| Enum values (in DB + JSON) | `UPPER_SNAKE_CASE` | `'DRAFT'`, `'PUBLISHED'`, `'PENDING_REVIEW'` |
| Indexes | `idx_<table>_<cols>` | `idx_listings_city` |
| Unique indexes / constraints | `uq_<table>_<cols>` | `uq_users_email` |
| Foreign keys | `fk_<table>_<col>` | `fk_listing_media_listing_id` |
| Flyway migration files | `V{yyyyMMddHHmmss}__{snake_case_description}.sql` (post Phase 7) | `V20260530101500__add_listing_audit_fields.sql` |

### Repositories (two-layer — target, Phase 2)

```java
// Custom interface — services depend on THIS
public interface IUserRepository {
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Page<User> findActiveWithFilters(String search, Pageable pageable);
}

// Spring Data implementation — extends BOTH JpaRepository and the custom interface
public interface UserRepository extends JpaRepository<User, String>, IUserRepository {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmailAndIsDeletedFalse(@Param("email") String email);
}
```

- All queries must respect soft deletes (`isDeleted = false`) once Phase 4 lands.
- Return `Optional<T>` for single results, `Page<T>` for paginated.
- Native queries are allowed but kept rare — prefer JPQL for portability.

### DTOs

```java
// Request DTOs — validated with Jakarta annotations
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateListingRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255) private String title;

    @NotNull @DecimalMin("0") private BigDecimal rentAmount;
}

// Response DTOs — built with @Builder
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ListingResponse {
    private String id;
    private String title;
    private BigDecimal rentAmount;
    private Instant publishedAt;
}
```

- Use Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Jakarta Bean Validation on request DTOs — validation messages are user-facing.
- Response DTOs use `@Builder` for clean construction.
- Nested DTOs as static inner classes when tightly coupled.
- Never expose entity objects directly in API responses.

### Services

```java
public interface IListingService {
    ListingResponse createListing(CreateListingRequest request);
    ListingResponse getListing(String listingId);
}

@Service
public class ListingService implements IListingService {
    private final IListingRepository listingRepository;
    private final IUserRepository userRepository;

    public ListingService(IListingRepository listingRepository,
                          IUserRepository userRepository) {  // Constructor injection
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ListingResponse createListing(CreateListingRequest request) {
        // Business logic, validation, entity creation
    }
}
```

- **Constructor injection only** — never `@Autowired` on fields.
- **`@Transactional`** on methods that write data. `@Transactional(readOnly = true)` only on methods that truly perform no writes. **Mixing `readOnly = true` with any `save()` is a critical bug** — Postgres rejects writes on a read-only connection and Hibernate silently drops persists.
- Throw custom exceptions for business rule violations (`ValidationException`, `ResourceNotFoundException`, `ForbiddenException`).
- Use factory methods on exceptions where they exist: `AuthenticationException.invalidCredentials()`.
- Null-guard partial updates: `if (request.getField() != null) entity.setField(request.getField())`.

### Controllers

```java
@RestController
@RequestMapping("/api/v1/listings")
@Validated
public class ListingController {
    private final IListingService listingService;

    public ListingController(IListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_LISTING')")
    public ResponseEntity<ApiResponse<ListingResponse>> create(
            @Valid @RequestBody CreateListingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Listing created",
                listingService.createListing(request)));
    }
}
```

- **Zero business logic** — controllers only route, authorize, and delegate.
- All endpoints return `ResponseEntity<ApiResponse<T>>` or `ResponseEntity<PaginatedResponse<T>>`.
- `@PreAuthorize` for method-level authorization. Permission-based authorities land in Phase 6; until then, role-based checks (`hasRole('ADMIN')`) are acceptable, but methods must still be annotated explicitly.
- `@Valid` on request bodies; `@Min`/`@Max` on query params (requires `@Validated` on the class).

### Pagination (target — Phase 5)

- **Every list endpoint that can grow must be paginated** — server-side via `PaginatedResponse<T>`.
- Non-paginated list endpoints are only acceptable for small, bounded datasets (e.g. amenity dropdowns).
- Pattern: `GET /resource?page=1&perPage=10&search=...&filterField=...`
- `@RequestParam(defaultValue = "1") @Min(1) int page` and `@RequestParam(defaultValue = "10") @Min(1) @Max(100) int perPage`.
- Service builds `PageRequest.of(page - 1, perPage)` (Spring is 0-indexed, the API is 1-indexed).
- Return `PaginatedResponse.from(resultPage, page)` with `items`, `currentPage`, `totalPages`, `totalItems`, `perPage`.
- Supported sort values for public listing browse: `PUBLISHED_AT_DESC`, `RENT_AMOUNT_ASC`, `RENT_AMOUNT_DESC`, `CREATED_AT_DESC`.

### Exception Handling

All custom exceptions extend `ApiException` (which extends `RuntimeException`):

- **`ValidationException`** — `400 BAD_REQUEST` for business rule / input violations
- **`ResourceNotFoundException`** — `404 NOT_FOUND`
- **`AuthenticationException`** — `401 UNAUTHORIZED`
- **`ForbiddenException`** — `403 FORBIDDEN`
- **`RateLimitExceededException`** — `429 TOO_MANY_REQUESTS`

`GlobalExceptionHandler` (`@RestControllerAdvice`) catches everything and returns:

```json
{ "success": false, "code": "VALIDATION_ERROR", "message": "...", "errors": {}, "timestamp": "..." }
```

- Use static factory methods where they exist: `AuthenticationException.invalidCredentials()`.
- Jakarta validation errors are auto-mapped to a field-level error map.
- Never catch exceptions silently — let them propagate to the global handler.

### Common Utilities

- **`ApiResponse<T>`** — `@JsonInclude(NON_NULL)`. Factory methods: `ApiResponse.ok(data)`, `ApiResponse.ok(message, data)`.
- **`PaginatedResponse<T>`** — Built from Spring `Page<T>` via `PaginatedResponse.from(page, requestedPage)`.
- **`IdGenerator`** (post Phase 3) — Thread-safe UUID v7: `IdGenerator.newId()`. Used by `BaseEntity.onCreate()`.
- **`SecurityUtils`** — Static helpers: `requireCurrentUserId()`, `hasRole()`, `currentUserOrNull()`.

### Security

- JWT-based stateless authentication. Access + refresh token pair.
- `JwtAuthenticationFilter` validates tokens on every request.
- Roles: `RENTER`, `AGENT`, `LANDLORD`, `ADMIN`.
- Method-level authorization via `@PreAuthorize("hasAuthority('<PERMISSION>')")`. Permissions are defined in `com.rentalapp.security.Permission` and mapped to roles via `Permission.forRole(...)`. See `docs/Permission_Catalog.md` for the full catalog. `hasRole(...)` checks still work (the principal also emits `ROLE_<role>`) but new code should use `hasAuthority(...)`.
- `SecurityConfig` disables CSRF, enables CORS, stateless sessions.
- BCrypt password hashing with strength factor managed by Spring's default `BCryptPasswordEncoder`.
- Backend-enforced role and ownership rules — never trust client-provided role or ownership.

#### Agent recommendation rules

- Only authenticated users can submit.
- Self-recommendations are blocked.
- Admin users cannot submit public recommendations.
- One recommendation per author per agent in MVP.

#### Auth-flow safety rules

- **`@Transactional(readOnly = true)` is forbidden on any method that persists a token, audit row, or any other write** — Postgres + Hibernate silently drop the INSERT on a read-only connection, leaving you with an unusable token in the JWT and no DB row. All login / refresh / logout / password-reset methods that touch `refresh_tokens` or `password_reset_tokens` must be `@Transactional` (writable).
- Integration tests (real DB, not mocked repos) must cover the full `login → refresh → logout` cycle to prevent regressions of the above.

### Code Quality Rules

#### Logging Policy

- Only add logs for critical errors that need debugging.
- Never add debug/trace logs in production code.
- Never log sensitive data (passwords, tokens, PII, payment-like data).
- Use MDC context (requestId, clientIp) for log correlation when added.

#### Testing

- Every feature module must have tests.
- Unit tests: `*Test.java` (run with surefire) — `@WebMvcTest` for controllers, Mockito mocks for collaborators.
- Integration tests: `*IT.java` (run with failsafe) — `@SpringBootTest` + a real DB (Testcontainers or the local Postgres) + real JWT signing.
- **Tests must assert observable behavior, not echo their own setup.** A test whose only assertion is `verify(mock).method()` *or* that re-asserts a value that came from `when(...).thenReturn(...)` is a tautology — it passes whether the production code is correct or not. Assert on: values the service *derived* (not stubbed), exception types and messages, persisted entity state, JWT contents, response status + body **shape** (e.g. tokens absent on `/me`, not values copied through). Reserve `verify(...)` for side-effects (`repository.save`, `tokenBlacklist.save`, etc.).
- **What controller (`@WebMvcTest`) tests are for**: validation (400 on bad input, correct field-level error code), authorization (401/403 on missing or insufficient authority), routing/content-type, response-shape contracts. Do not assert payload values that originate from a mocked service — that's serialization plumbing, not your contract.
- When a single mock-heavy unit test is testing many internal-collaborator interactions for one user-visible flow, prefer one `*IT.java` that exercises real DB state and real JWT signing. Strong candidates here: the entire auth flow (login → refresh → logout), listing publish + moderation, recommendation submission rules.

#### File & Method Length

Line counts are heuristics to trigger a refactor conversation — the real signal is **responsibility count**.

**Per-file targets (by layer):**
- **Controllers** — < 200 lines. Pure routing + `@PreAuthorize` + delegation.
- **Services** — 200–400 lines ideal, 500 soft ceiling. Split when handling multiple sub-domains.
- **Repositories** — < 200 lines.
- **Entities** — < 200 lines. No business logic.
- **DTOs** — < 150 lines.
- **Hard ceiling: ~1,000 lines** for any Java file — almost always a God class.

**Per-method targets:**
- **Ideal: 5–20 lines.**
- **Soft ceiling: 30 lines** — extract helpers beyond this.
- **Hard ceiling: 50 lines** — refactor before merging.

**Signals (not line counts):**
- A class name needs "and" to describe it → split.
- Private helpers outnumber public methods 3:1 → those helpers may be a separate collaborator.
- A method has > 3 levels of nesting → extract or invert with early returns.

**What NOT to do:**
- Don't split a file just to hit a line count — cohesive code beats scattered code.
- Don't extract a one-call private method "for cleanliness" — inline beats premature abstraction.

#### General Conventions

- **No field injection** — constructor injection only.
- **No `@Autowired`** — Spring resolves single-constructor beans automatically.
- **Proper imports** — never use fully-qualified class names inline. Add imports at the top.
- **Builder pattern** for response DTOs, constructor for entities.
- **Normalize input** — trim and lowercase emails, normalize blank strings to null.
- **Soft deletes** — never hard-delete domain entities. Use `entity.setDeleted(true); entity.setDeletedAt(Instant.now()); entity.setDeletedBy(SecurityUtils.requireCurrentUserId());`. Reads are auto-filtered by Hibernate `@SQLRestriction("is_deleted = false")` on each entity. **Exceptions** (kept as hard-delete or excluded from the restriction): `SavedListing` (unfavorite is a user action that should disappear, not be preserved), `RefreshToken`/`PasswordResetToken` (have their own lifecycle: `revokedAt`/`consumedAt`), `ModerationAction`/`AiRequestLog` (immutable audit logs).
- **Optimistic locking** (post Phase 3) — `@Version` on all entities prevents lost updates.
- **Idempotent migrations** — use `IF EXISTS` / `IF NOT EXISTS` guards in SQL migrations.

### Feature Development Flow

1. **Design entity** — fields, relationships, types.
2. **Write Flyway migration** — translate entity into SQL (`CREATE TABLE`).
3. **Entity class** — annotate with JPA mappings, extend `BaseEntity`.
4. **Repository** — custom interface (`IFooRepository`) + Spring Data impl (`FooRepository`).
5. **DTOs** — request (with Jakarta validation) + response (with `@Builder`).
6. **Service interface + implementation** — business logic, transactions, exception handling.
7. **Controller** — thin routing, `@PreAuthorize`, delegation.
8. **Tests** — controller tests with `@WebMvcTest` for validation/authz/shape; integration test (`*IT.java`) for the end-to-end flow against a real DB.

### Schema Changes

- **NEVER modify an existing migration file** — once applied, it's immutable. Editing a migration and running `flyway:repair` only fixes the checksum; it does NOT re-run the SQL.
- **Always use the script to create migrations:** `./scripts/new-migration.sh "description here"` — it generates timestamp-based version numbers (`YYYYMMDDHHmmss` UTC) so migrations from different branches never collide.
- Each migration is an incremental change.
- If you need to fix a mistake in an applied migration, create a NEW migration with the corrective DDL.

### Backend Commands

```bash
./mvnw clean compile
./mvnw test
./mvnw verify
./mvnw spring-boot:run
```

### Current truths to preserve

#### Current media contract

- Listing media is stored on the backend filesystem and served from `/media/...`.
- An upload endpoint exists: `POST /api/v1/listings/media/upload` (multipart, 5 MB cap, JPEG/PNG/WebP).
- Media URLs are attached to listings via listing create/update payloads.

#### Current AI contract

- AI enhancement is **advisory only**.
- AI failure must not block listing save/publish workflows.
- Heuristic provider only — no Spring AI / Ollama / Qdrant integration in code yet.
- Do not document Qdrant/Ollama integration as present unless code actually wires it.

#### Current search contract

- Public listing browse supports `page` (1-indexed, default 1, `@Min(1)`), `perPage` (default 10, `@Min(1) @Max(100)`), and `sort`.
- Supported sort values: `PUBLISHED_AT_DESC`, `RENT_AMOUNT_ASC`, `RENT_AMOUNT_DESC`, `CREATED_AT_DESC`.
- Response wraps results in `PaginatedResponse<T>` with fields: `items`, `currentPage`, `perPage`, `totalItems`, `totalPages`, `hasNext`, `hasPrevious`, `sort`.

---

## Frontend

### Technology Stack

- Next.js 16 (App Router)
- React 19, TypeScript
- Material UI (MUI) v7
- Zustand for state, Formik + Yup for forms
- Axios (with `axios-retry`)
- Biome for linting + formatting

### Project Structure

```
src/
  app/                  → Next.js App Router routes (route folders + page.tsx)
  layouts/              → Layout wrappers (Dashboard shell, public shell, etc.)
  components/
    common/             → Shared/reusable components
    {feature}/          → Feature-specific components (auth/, listings/, admin/)
  features/             → Feature-scoped logic, hooks, sub-components
  hooks/                → Custom React hooks (useAuth, useListings, etc.)
  lib/
    api/
      client.ts         → Axios instance (base URL, interceptors, token refresh)
      {feature}.ts      → API service functions (listings.ts, auth.ts, …)
    auth/
      sessionStore.ts   → Shared browser session state
  stores/               → Zustand stores
  theme/                → MUI theme configuration
  types/                → TypeScript interfaces grouped by domain (auth.ts, listings.ts)
  validations/          → Yup schemas grouped by domain
```

### Architecture Rules

- **Routes are thin**: `src/app/**/page.tsx` only imports and renders a feature component from `components/` or `features/`. No business logic in route files.
- **Data flow**: `Route → Component → Hook → API service (lib/api/) → Backend`.
- **Types go in `types/`** — never define interfaces inline in components.
- **Validation goes in `validations/`** — Yup schemas are separate from types. Narrow component-local schemas (≤5 fields, single-component use) are acceptable inline; any reused domain schema must live in `validations/`.
- **Stores go in `stores/`** — Zustand stores are standalone files; hooks consume them.
- **API services go in `lib/api/`** — pure functions, no React. All use the shared `client.ts` Axios instance.
- Protected API requests may refresh once on `401` and retry once. Failed refresh clears the stored session and degrades cleanly to re-authentication.

### Styling Rules

- Use MUI's `sx` prop or `styled()` for all styling. Never Tailwind, never raw CSS.
- **All components must use MUI** — no Tailwind.
- **Border radius**: Global `borderRadius: 8` in the theme. Do NOT add manual border-radius via `sx` unless overriding.
- **Elevation**: `elevation: 0` is global. Do NOT add `elevation={0}` manually.
- **Responsive design**: All pages MUST be mobile-responsive. Use MUI responsive props/breakpoints. Never use fixed widths that break on mobile.

### Conventions

- Use named exports for hooks, default exports for components.
- Import MUI components from specific paths: `import Button from "@mui/material/Button"` (not `import { Button } from "@mui/material"`).
- Use `@/` path alias for all imports.
- Environment variables read via `process.env.NEXT_PUBLIC_*` (Next.js convention).
- Server components are the default in App Router; mark client components with `"use client"` explicitly only where needed (state, effects, browser APIs).

### Reusable Form Components

Always use these wrappers instead of raw `TextField` / `Select` / `DatePicker` in forms:

- **`CustomTextField`** — Formik-wired text field.
- **`CustomSelect`** — Formik-wired select.
- **`CustomMultiSelect`** — Multi-select with chip display and checkbox menu items.
- **`CustomButton`** — Button with built-in loading spinner.
- **`CustomDatePicker`** — Formik-wired date picker using `@mui/x-date-pickers`. Never use `<TextField type="date" />` or raw `<DatePicker>`.
- **`CustomSearchTextField`** — Standalone search input (no Formik). Pill-shaped with search icon.
- **`CustomDataGrid`** — Wraps `@mui/x-data-grid`. Supports `loading`, `searchable`, `title`.

If a wrapper does not yet exist for a control you need, create it in `components/common/` rather than reaching for raw MUI in feature code.

### Frontend Commands

```bash
npm run dev
npm run build
npm run lint
npm run format
```

---

## Shared Rules

### Role Policy

- `RENTER` — browses listings, saves, sends inquiries, submits public recommendations on agents.
- `AGENT` — manages own listings, receives inquiries, has a public profile that receives recommendations.
- `LANDLORD` — manages own listings, receives inquiries.
- `ADMIN` — moderation, user status, recommendation approval. Admins cannot submit public recommendations or create listings.

Backend enforces every role and ownership rule; the frontend role display is purely cosmetic.

### Compact Code Style

- Write concise, compact code. Avoid unnecessary vertical whitespace and verbose patterns.
- Keep methods/components short and focused — extract helpers when they grow beyond ~30 lines.
- Prefer inline conditionals and early returns over deeply nested if/else.
- Chain builder calls / inline props on fewer lines when they fit within ~120 characters.
- Avoid redundant comments that restate what the code already says.
- **Never one-prop-per-line** in JSX unless the component has complex expressions or exceeds ~120 chars.

### Development Workflow

#### Step 1: Plan Before Implementing

- **Every non-trivial task starts with a plan.** Identify affected files, outline the approach, consider edge cases.
- For bug fixes: investigate root cause first, then plan the fix. Never jump straight to code changes.
- For refactors: map all usages and downstream impacts before touching code.

#### Step 2: Implement

- Follow all conventions documented in this file strictly.
- For complex tasks, delegate to a domain-matched senior engineer agent (backend / frontend / full-stack).

#### Step 3: Critic / Fix Cycle

- **After every implementation or fix**, run the critic/fix cycle:
  1. Critic: reviews all new/changed code against `CLAUDE_REVIEW.md` — security, correctness, architecture, edge cases, conventions.
  2. Fix: addresses every issue identified.
- No implementation is complete until it passes this cycle.

#### Step 4: Verify

- **Backend (mandatory)**: After every backend change, run BOTH:
  1. `./mvnw clean compile` (build must pass)
  2. `./mvnw test` (full test suite must pass)
  Neither is optional. A backend change is not complete until both succeed.
- **Frontend (mandatory)**: After every frontend change, run `npm run build` — must pass.

### Documentation discipline

When behavior changes:

- update `docs/MVP_Data_Model.md`
- update `docs/API_Contract_V1.md`
- update `docs/Frontend_Route_Map.md`
- update `plan.md`
- update repo READMEs when setup or current-state claims change

The docs must remain trustworthy.

### Decision heuristics

Prefer:

- precise product naming over overloaded terms
- simpler implementation over speculative architecture
- honest documentation over aspirational documentation
- real rental workflow value over peripheral feature creep
