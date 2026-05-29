# CLAUDE_REVIEW.md

> Claude reads this file directly. Tell Claude: *"Read CLAUDE_REVIEW.md then review [files / module / diff]."*

---

## 1. PROJECT CONTEXT

- **Project:** RentalApp — rental house hunting platform (rentals only)
- **Language:** Java 21
- **Framework:** Spring Boot 3.4
- **Database:** PostgreSQL 16 (Flyway migrations, snake_case columns, VARCHAR enums)
- **Authentication:** JWT (stateless, access + refresh tokens). Role-based today (`RENTER`, `AGENT`, `LANDLORD`, `ADMIN`); permission-based authorities target after Phase 6.
- **Architecture:** Package-by-feature (`module/<feature>/` containing entity, repository custom interface + Spring Data impl, service interface + impl, controller, DTOs)
- **Frontend:** Next.js 16 (App Router) + React 19 + TypeScript + MUI v7 + Zustand + Formik + Yup + Axios + Biome
- **Other:** Lombok, Jakarta Bean Validation, UUID v7 IDs (post Phase 3), soft deletes (post Phase 4), optimistic locking (post Phase 3)

Migration status by phase is tracked at the top of `CLAUDE.md` — apply the **target** convention from `CLAUDE.md` when reviewing, and flag deviations even if other code in the repo hasn't been migrated yet (call them out as "pre-migration legacy" rather than as new violations).

---

## 2. ARCHITECTURE RULES

### Package-by-Feature Structure

```
module/<feature>/
  controller/
    └── <Feature>Controller.java   → Thin HTTP layer (routing + @PreAuthorize only)
  dto/
    └── *Request.java              → Request DTOs (Jakarta validation)
    └── *Response.java             → Response DTOs (@Builder)
  entity/
    └── <Entity>.java              → JPA entity extending BaseEntity
  repository/
    └── I<Entity>Repository.java   → Custom repository interface (services depend on this)
    └── <Entity>Repository.java    → Spring Data impl (extends JpaRepository + custom interface)
  service/
    └── I<Feature>Service.java     → Service interface (focused contract per feature)
    └── impl/<Feature>Service.java → Service implementation (all business logic)

config/      → Spring configuration classes
security/    → JWT filter, SecurityConfig, SecurityUtils
exception/   → ApiException hierarchy, GlobalExceptionHandler
common/      → BaseEntity, ApiResponse, PaginatedResponse, IdGenerator
```

### Strict Layer Violations to Flag

- Controller containing ANY business logic (must only route, authorize, delegate)
- Controller calling a repository directly (must go through a service)
- Entity returned from a controller or service method (must be mapped to a response DTO)
- Repository injected directly into a controller (must go through service interface)
- Service depending on a concrete repository class instead of `I<Name>Repository`
- Business logic inside a repository query (belongs in service)
- Field injection (`@Autowired`) instead of constructor injection
- Service not implementing its `I<Name>Service` interface
- Entity not extending `BaseEntity`
- Custom exception not extending `ApiException`
- `@Transactional(readOnly = true)` on a method that calls `save(...)`, `delete(...)`, or otherwise persists state — **CRITICAL** bug (Postgres + Hibernate silently drop the write)
- New `EAGER` `@ManyToOne` / `@OneToMany` — must be `LAZY`
- Hard-delete operations (`repository.delete(...)`) on domain entities — must be soft delete (`setDeleted(true)` + `setDeletedAt(now)` + `setDeletedBy(currentUserId)`). Exempt: `SavedListing` (hard-delete intentional), token tables (own lifecycle), audit logs (immutable).
- Adding `@SQLRestriction("is_deleted = false")` to a token/audit entity (RefreshToken, PasswordResetToken, ModerationAction, AiRequestLog) — those are deliberately excluded from soft-delete semantics.
- Native SQL queries on soft-deleted entities that don't include `is_deleted = false` — `@SQLRestriction` doesn't apply to raw native queries; must be added manually.

### Two-Layer Repository Pattern

```java
// Custom interface — services depend on THIS
public interface IListingRepository {
    Optional<Listing> findByIdAndIsDeletedFalse(String id);
    Page<Listing> findPublishedWithFilters(String city, BigDecimal maxRent, Pageable pageable);
}

// Spring Data implementation — extends BOTH JpaRepository and the custom interface
public interface ListingRepository extends JpaRepository<Listing, String>, IListingRepository {
    @Query("SELECT l FROM Listing l WHERE l.id = :id AND l.isDeleted = false")
    Optional<Listing> findByIdAndIsDeletedFalse(@Param("id") String id);
}
```

### Frontend Architecture (Next.js App Router)

```
src/app/                → App Router routes — page.tsx files render feature components, no logic
src/components/{feature}/ → Feature-specific components (all UI logic lives here)
src/components/common/  → Shared reusable components (CustomButton, CustomTextField, etc.)
src/features/           → Feature-scoped hooks / sub-components when components/ would get crowded
src/hooks/              → Custom React hooks (useAuth, useListings, etc.)
src/lib/api/            → API service functions (pure functions, no React)
src/lib/api/client.ts   → Shared Axios instance with interceptors + refresh-on-401
src/lib/auth/           → sessionStore + helpers
src/stores/             → Zustand stores
src/types/              → TypeScript interfaces grouped by domain
src/validations/        → Yup schemas grouped by domain
```

### Frontend Violations to Flag

- Business logic inside `src/app/**/page.tsx` (must be in `components/` or `features/`)
- TypeScript interfaces defined inline in components (must be in `types/`)
- Yup validation schemas defined inline for reused domain objects (must be in `validations/`). Narrow component-local schemas (≤5 fields, single-component use) are acceptable inline.
- Raw MUI `TextField` / `Select` / `DatePicker` used instead of the `Custom*` wrappers
- Barrel imports from MUI (`import { Button } from "@mui/material"` — must use `import Button from "@mui/material/Button"`)
- Tailwind classes or raw CSS (must use MUI `sx` prop or `styled()`)
- Hardcoded error messages on the frontend (must come from backend via `extractApiError(err)`)
- API calls made directly in components (must go through `lib/api/` service functions)
- Zustand store logic inside components (must be in `stores/`)
- `"use client"` directive applied unnecessarily (only mark client components when they need state/effects/browser APIs)

---

## 3. CODING STANDARDS

### Security

- Passwords MUST be BCrypt encoded — never plain text.
- NEVER log passwords, tokens, OTPs, or any sensitive user data (PII).
- All endpoints must be protected via `@PreAuthorize("hasAuthority('...')")` or `hasRole('...')` EXCEPT `/api/v1/auth/**` and explicitly public endpoints (listing browse, agent recommendations read).
- Input MUST be validated using `@Valid` on request bodies, `@Min`/`@Max` on query params (requires `@Validated` on the controller class).
- Never expose internal stack traces or exception messages in API responses — `GlobalExceptionHandler` handles all errors uniformly.
- Secrets and credentials must come from environment variables — never hardcoded.
- JWT tokens validated on every request via `JwtAuthenticationFilter`.
- Use `SecurityUtils.requireCurrentUserId()` for auth context — never trust client-provided user IDs for authorization.
- Backend-enforced role and ownership rules; the frontend's claim about a user's role is purely cosmetic.

### Auth-flow specific rules (post-bug-fix)

- `@Transactional(readOnly = true)` is FORBIDDEN on any method that persists a token, audit row, or any other write. Postgres + Hibernate silently drop the INSERT on a read-only connection. **Always flag this as CRITICAL.**
- `login`, `refresh`, `logout`, `confirmPasswordReset` must all be `@Transactional` (writable).
- The refresh-token rotation policy: on `refresh`, the old token is revoked (`revokedAt = now`) and a new token is issued. Logout revokes the current token. Suspended account on refresh forces revocation and throws.
- Integration tests (`*IT.java`) must cover the `login → refresh → logout` cycle against a real DB. Unit tests with mocked `RefreshTokenRepository` are insufficient — they cannot catch the read-only-write bug class.

### Spring Boot Best Practices

- **`@Transactional`** on all service write methods; `@Transactional(readOnly = true)` only on methods that genuinely perform no writes.
- **`@Validated`** on controllers for `@Min`/`@Max` query param validation.
- All list/search endpoints MUST support server-side pagination via `Page<T>` + `PaginatedResponse.from()`.
- Pagination params (post Phase 5): `page` (default 1, 1-indexed), `perPage` (default 10, max 100).
- Use `GlobalExceptionHandler` (`@RestControllerAdvice`) for centralized exception handling.
- Use custom exceptions extending `ApiException` — never throw generic `RuntimeException`.
- Use static factory methods on exceptions where they exist: `AuthenticationException.invalidCredentials()`, `ResourceNotFoundException::new`.
- Never use `Optional.get()` — use `orElseThrow()` with a meaningful exception.
- **Constructor injection only** — no `@Autowired` field injection, no setter injection.
- Spring auto-resolves single-constructor beans — no `@Autowired` annotation needed on constructors.
- All endpoints return `ResponseEntity<ApiResponse<T>>` or `ResponseEntity<PaginatedResponse<T>>`.

### Entity Rules

- ALL entities extend `BaseEntity` (post Phase 3 provides: UUID v7 `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `isDeleted`, `deletedAt`, `deletedBy`, `version`).
- **LAZY fetch everywhere** — never `FetchType.EAGER`. Use `@EntityGraph` or JOIN queries when needed.
- **snake_case columns** — Spring/Postgres default. Plain (unquoted) identifiers.
- Enums: `@Enumerated(EnumType.STRING)` stored as `VARCHAR`. Not Postgres native enums.
- **Soft deletes only (post Phase 4)** — never hard-delete. Set `isDeleted = true`, `deletedAt`, `deletedBy`.
- **Optimistic locking (post Phase 3)** — `@Version` on all entities via `BaseEntity`.
- Lombok on entities: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`.
- Response DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.
- Request DTOs: `@Data @NoArgsConstructor @AllArgsConstructor` with Jakarta validation annotations.

### Database & Migrations

- **NEVER modify an existing migration file** — create a NEW migration for corrections.
- Pre-Phase-7: migrations are integer-numbered (`V1`..`V13`). Post-Phase-7: use `./scripts/new-migration.sh "description"` for timestamp-versioned names.
- Each migration is an incremental change (e.g. `ALTER TABLE ... ADD COLUMN`).
- Use `IF EXISTS` / `IF NOT EXISTS` guards for idempotent migrations.
- All queries must respect soft deletes (`is_deleted = false`) after Phase 4.
- Return `Optional<T>` for single results, `Page<T>` for paginated results.

### Frontend Standards

- Next.js App Router — `src/app/**/page.tsx` are route entry points; they must be thin.
- Use `@/` path alias for all imports.
- Named exports for hooks, default exports for components.
- Always use the `Custom*` form wrappers (`CustomTextField`, `CustomSelect`, `CustomMultiSelect`, `CustomButton`, `CustomDatePicker`, `CustomSearchTextField`, `CustomDataGrid`).
- MUI `sx` prop for all styling — no Tailwind, no raw CSS.
- Global `borderRadius: 8` — don't add manual border-radius via `sx` unless overriding.
- Global `elevation: 0` — don't add `elevation={0}` manually.
- All pages MUST be mobile-responsive using MUI responsive props and breakpoints.
- Error messages: always use `extractApiError(err)` — never hardcode error strings.
- Server vs client components: default to server components; mark client components with `"use client"` only when state/effects/browser APIs are required.
- Environment variables read via `process.env.NEXT_PUBLIC_*` for public values; private values stay server-side and never reach the client bundle.

### Role Policy

- `RENTER` — browses, saves, inquires, recommends agents.
- `AGENT` — manages own listings; receives inquiries; receives public recommendations.
- `LANDLORD` — manages own listings; receives inquiries.
- `ADMIN` — moderation, user status, recommendation approval. Admins do NOT create listings or recommend agents.

### General Code Quality

- No magic numbers or hardcoded strings — use constants or enums.
- Methods do one thing (Single Responsibility).
- No method should exceed ~30 lines — extract helpers.
- No god classes.
- Remove all unused imports, variables, and dead code.
- Normalize input: trim and lowercase emails, normalize blank strings to null.
- Only log critical errors needed for debugging — no debug/trace logs in production.
- Use MDC context (requestId, clientIp) for log correlation when added.

---

## 4. AI SLOP PATTERNS TO FLAG

Signs that code was AI-generated without proper review:

- **Obvious comments** restating what the code clearly says:
  ```java
  // Bad: Get the user by id
  User user = userRepository.findById(id);
  ```
- **Generic variable names** — `data`, `result`, `temp`, `obj`, `response`, `item`.
- **Redundant null checks** — null checking something already validated upstream by Jakarta or the framework.
- **Duplicate code** — same logic copy-pasted across multiple methods.
- **Over-engineering** — complex abstractions for a simple problem.
- **Unnecessary try/catch** — catching exceptions and doing nothing meaningful, or rethrowing without adding value.
- **Unused methods or variables** — code that exists but is never called.
- **Inconsistent naming** — mixing conventions within the same module.
- **Large commented-out blocks** — dead code left behind.
- **Verbose patterns** — one-prop-per-line JSX when inline fits under ~120 chars; unnecessary intermediate variables.
- **Placeholder/stub implementations** — TODO comments, empty methods, hardcoded mock data left in production code.
- **Defensive over-engineering** — excessive input validation for internal code paths already guaranteed safe by the framework.
- **Tautological tests** — assertions that re-state values set up by `when(...).thenReturn(...)`, or whose only assertion is `verify(mock).method()` for a side-effect that the production code can't avoid making (so the test passes whether the code is correct or not). Real assertions: persisted state, derived values, exception type/message, response shape, JWT contents.

---

## 5. REVIEW INSTRUCTIONS FOR CLAUDE

You are a senior Java/Spring Boot + TypeScript/React engineer doing a thorough code review for RentalApp.

Use all the rules defined in sections 2, 3, and 4 above to review the code provided.

### What to Look For

1. **Bugs** — logic errors, null pointer risks, broken transactions (especially `readOnly = true` on writes), improper exception handling, N+1 queries, race conditions.
2. **Security vulnerabilities** — exposed data, missing auth, hardcoded secrets, injection risks, missing `@PreAuthorize`, trusting client-provided IDs.
3. **Architecture violations** — anything breaking package-by-feature, two-layer repo pattern, layer boundaries, frontend data flow.
4. **Bad practices** — soft delete violations, missing constructor injection, DTO exposure, etc.
5. **AI slop** — any patterns from section 4, especially tautological tests.
6. **Performance** — N+1 queries, EAGER fetching, missing pagination, unbounded result sets.
7. **Convention violations** — naming, file placement, import style, Lombok usage, MUI usage.

### Report Format

Return findings using this exact structure:

```
## Review Report: [FileName.java / Component.tsx]

---
[SEVERITY EMOJI] [Issue Title]
- File:   FileName.java
- Line:   42
- Method: methodName()
- Issue:  Clear description of the problem
- Why:    Why this is a problem in this project specifically
- Fix:    Exact code change or clear instruction to fix it
---
```

Severity levels:

- 🟥 **CRITICAL** — Security vulnerability, data loss risk, or broken functionality. Fix immediately. (Example: `@Transactional(readOnly = true)` on a method that writes — silently drops the INSERT.)
- 🟧 **IMPORTANT** — Architecture violation, correctness bug, or performance issue. Fix before merge.
- 🟨 **MINOR** — Convention violation, slop, or style issue. Fix when possible.

### Rules for the Review

- Be **specific** — exact method names, line numbers, what's wrong.
- Be **honest** — if code is correct and well written, say so.
- Do **not** invent issues — only flag real problems.
- Group all issues **by file** for easy navigation.
- Flag pre-existing issues separately from new code issues if reviewing a diff.
- Migration-status awareness: if the violation is something a pending migration phase (per `CLAUDE.md`) will fix, mark it as "**pending Phase N**" rather than treating it as a new violation.
- At the end, give a summary:
  ```
  Summary:
  - CRITICAL issues: N
  - IMPORTANT issues: N
  - MINOR issues: N
  - Overall: Needs work before production / Good with minor fixes / Production ready
  ```

---

## 6. HOW TO USE THIS FILE

Claude reads this file directly — no copy-pasting needed. Tell Claude what to review.

### Example Prompts

**Full module review:**
> Read CLAUDE_REVIEW.md then review the entire `module/listings/` module.

**Specific files:**
> Read CLAUDE_REVIEW.md then review `AuthServiceImpl.java` and `AuthController.java`.

**Review a diff / recent changes:**
> Read CLAUDE_REVIEW.md then review all changes on the current branch vs main.

**Targeted review (focus area):**
> Read CLAUDE_REVIEW.md then review `SecurityConfig.java` focusing on security.
> Read CLAUDE_REVIEW.md then review the listings module focusing on performance.

**Frontend review:**
> Read CLAUDE_REVIEW.md then review `src/app/listings/page.tsx` and `src/lib/api/listings.ts`.

Claude will read the referenced files, apply all rules from sections 2–4, and return findings in the report format from section 5.
