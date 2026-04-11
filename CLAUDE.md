# Claude Code Instructions for RentalApp

Rental house hunting platform focused on long-term rental discovery. This file is the working coding guide for the backend repo and should stay aligned with the sibling frontend repo:

- Backend repo: `RentalApp_backend/`
- Frontend repo: `RentalApp_Frontend/`

Primary planning documents in this repo:

- [Rental_App_Final_Detailed_BRD.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\Rental_App_Final_Detailed_BRD.md)
- [docs/MVP_Data_Model.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\MVP_Data_Model.md)
- [docs/API_Contract_V1.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\API_Contract_V1.md)
- [docs/Frontend_Route_Map.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\Frontend_Route_Map.md)
- [plan.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\plan.md)

---

## Product Boundaries

The MVP is for:

- renter registration and login
- renter, agent, landlord, and admin roles
- profile management
- rental listing creation and management
- public listing browse and filtering
- listing detail pages
- saved listings
- inquiry workflows
- recommendations for agents
- reporting and moderation
- AI-assisted listing enhancement

The MVP is not for:

- payments
- tenancy agreement workflows
- live chat
- native mobile apps
- full geospatial maps
- advanced AI matching or fraud scoring
- property sales or land sales

If a proposed implementation expands beyond the MVP, stop and justify it before coding.

---

## Architecture

### Backend stack

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring AI
- Ollama integration later in MVP

### System design

- Build as a modular monolith
- Package by feature, not by technical layer only
- Keep module boundaries clear from the start
- AI must remain optional and non-blocking

### Planned backend modules

- `auth`
- `profiles`
- `listings`
- `saved`
- `inquiries`
- `recommendations`
- `admin`
- `media`
- `ai`

Shared infrastructure should live under folders such as:

- `config`
- `security`
- `exception`
- `common`

---

## Delivery Approach

Follow the module order in [plan.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\plan.md):

1. Shared foundations
2. Auth
3. Profiles
4. Listings core
5. Search and public discovery
6. Saved listings
7. Inquiries
8. Media
9. Recommendations
10. Reports and admin moderation
11. AI assist

Rules:

- Build vertical slices across backend and frontend
- Do not jump ahead to later modules when earlier dependencies are unstable
- Frontend mocks are allowed temporarily, but replace them before a module is considered complete
- Keep docs updated when contracts or data structures change

---

## API Rules

### API shape

- Base path is `/api/v1`
- All request and response shapes should stay aligned with [docs/API_Contract_V1.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\API_Contract_V1.md)
- Do not expose JPA entities directly in responses
- Use DTOs for both requests and responses

### Error handling policy

- User-facing error messages should originate from the backend
- The frontend should display backend-provided messages rather than inventing conflicting ones
- Validation errors should come from Jakarta Bean Validation where possible
- Business rule violations should use typed custom exceptions and the global exception handler

### Response conventions

Use a consistent wrapper for success responses and a consistent error structure for failures. Once introduced, all controllers should follow the same pattern.

---

## Code Structure Rules

### Controllers

- Controllers are thin
- Controllers handle routing, validation, authorization, and response mapping
- Controllers do not contain business logic

### Services

- Services own business rules
- Services control transactions
- Services depend on interfaces or clean abstractions where useful

### Repositories

- Repositories handle persistence only
- Do not bury business rules inside repository queries
- Query methods should remain readable and explicit

### DTOs

- Request DTOs use Jakarta Bean Validation
- Response DTOs are explicit and stable
- Never return entity objects directly from controllers

### Entities

- Entities represent persistent domain state
- Keep entity logic minimal
- Use string-backed enums in persistence
- Default relationship loading to `LAZY`

### Dependency injection

- Constructor injection only
- Do not use field injection
- Do not use `@Autowired` on fields

---

## Data and Persistence Rules

The source of truth for the MVP schema is [docs/MVP_Data_Model.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\MVP_Data_Model.md).

### Entity and schema conventions

- Use UUID primary keys
- Persist enums as strings
- Use Flyway for all schema changes
- Do not manually change database structure outside migrations
- Prefer nullable fields only when the business model genuinely allows missing data

### Migration rules

- Every schema change must have a Flyway migration
- Migrations should be idempotent where practical
- Never rename or remove enum values casually
- Schema changes must reflect the documented domain model or explicitly update the docs

### Query and indexing rules

- Search-related fields must be indexed deliberately
- Public listing queries must exclude unpublished, archived, rejected, and disabled records as required by business rules
- Ownership and role restrictions must be enforced server-side, never only in frontend logic

---

## Security Rules

- Use JWT-based authentication
- Hash passwords with a secure algorithm such as BCrypt
- Protect all non-public endpoints
- Enforce role checks in backend code
- Enforce owner checks for user-owned resources
- Validate all upload inputs for type and size
- Never trust role or ownership information from the client

Public endpoints should remain limited to what the BRD allows, mainly listing browse, listing detail, and public profile views.

---

## Module-Specific Rules

### Auth

- One account has one primary role in MVP
- Admin creation is internal only
- Normalize emails before persistence and comparison

### Profiles

- Agent-only fields must not leak into renter behavior without validation
- Verification status is controlled by backend rules, not arbitrary client input

### Listings

- Only agents and landlords can create listings
- Only listing owners or admins can edit listing records
- Publish should enforce completeness rules
- Agent fee visibility is mandatory where applicable

### Search

- Only approved and published listings appear publicly
- Filters should be backed by real indexed fields

### Saved listings

- Only authenticated renters can save listings
- Duplicate saves should be prevented at both service and database level

### Inquiries

- Inquiries are always tied to one listing
- Sender and receiver visibility must be enforced
- Status transitions should remain simple for MVP

### Recommendations

- Recommendation abuse must be manageable by moderation
- Visibility should respect the moderation policy selected for MVP

### Reports and moderation

- Moderation decisions must be auditable
- Disabled listings must disappear from public search immediately

### AI assist

- AI is assistive only
- AI must never auto-publish content
- AI failure must not break listing creation or editing flows
- AI requests and outputs should be logged carefully

---

## Testing Expectations

Every module should include tests before it is marked complete.

### Backend test priorities

- controller validation behavior
- service business rules
- authorization and ownership checks
- repository queries for important search flows
- integration tests for critical module paths

### Minimum module quality bar

- happy path works
- obvious failure paths are covered
- unauthorized access is rejected
- invalid input is rejected

Do not leave testing until the end of the project.

---

## Logging and Observability

- Log critical operational events and failures
- Do not log passwords, tokens, or sensitive personal data
- AI request logging must balance observability with privacy
- Error logs should be useful enough to debug production issues without leaking secrets

---

## Documentation Discipline

When implementation changes the plan, schema, or contract:

- update `docs/MVP_Data_Model.md` if the domain model changes
- update `docs/API_Contract_V1.md` if endpoints or payloads change
- update `docs/Frontend_Route_Map.md` if route design changes
- update `plan.md` if module sequencing or scope changes materially

The docs should remain trustworthy. Do not let code and docs drift apart.

---

## Coding Conventions

- Prefer clear, explicit code over clever code
- Keep methods focused and short where practical
- Name classes and methods after business intent, not framework mechanics
- Use `BigDecimal` for monetary values
- Normalize and validate user input at boundaries
- Keep comments rare and useful
- Avoid premature abstractions unless multiple modules already need them

---

## Feature Development Flow

For each backend module:

1. Confirm the business rules in the BRD and `plan.md`
2. Update docs if the design changed
3. Write or update Flyway migration
4. Create or update entities
5. Create repositories
6. Create request and response DTOs
7. Implement service layer
8. Implement controller layer
9. Add tests
10. Integrate with frontend and remove temporary mocks

---

## Decision Heuristics

When unsure, prefer:

- simpler implementation over speculative extensibility
- explicit business rules over hidden conventions
- server-enforced validation over frontend-only validation
- module cohesion over shared utility sprawl
- delivery of core rental workflows over peripheral features

If a tradeoff conflicts with the BRD, the BRD wins unless deliberately revised.
