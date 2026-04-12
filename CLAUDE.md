# Claude Code Instructions for RentalApp

Rental house hunting platform focused on rentals only.

Backend repo:
- `RentalApp_backend/`

Sibling frontend repo:
- `RentalApp_Frontend/`

Primary project docs:

- [Rental_App_Final_Detailed_BRD.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\Rental_App_Final_Detailed_BRD.md)
- [docs/MVP_Data_Model.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\MVP_Data_Model.md)
- [docs/API_Contract_V1.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\API_Contract_V1.md)
- [docs/Frontend_Route_Map.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\Frontend_Route_Map.md)
- [plan.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\plan.md)

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

## Backend architecture

Stack currently in use:

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway

Current AI truth:

- lightweight assistive enhancement endpoint exists
- request logging exists
- full Spring AI + Ollama + Qdrant integration does not yet exist in code

System design rules:

- modular monolith
- package by feature
- clear module boundaries
- AI must remain optional and non-blocking

Preferred backend structure:

- `module/<feature>/controller`
- `module/<feature>/dto`
- `module/<feature>/entity`
- `module/<feature>/repository`
- `module/<feature>/service`
- `module/<feature>/service/impl`

Shared infrastructure:

- `config`
- `security`
- `exception`
- `common`

## Current backend modules

- `auth`
- `profiles`
- `listings`
- `saved`
- `inquiries`
- `recommendations`
- `suggestions`
- `admin`
- `reports`
- `media`
- `ai`

## Delivery approach

The original MVP slices have been implemented. New work should focus on:

1. documentation accuracy
2. admin/moderation UI completeness
3. deployment realism beyond local parity
4. search refinement beyond the current structured browse
5. AI/provider decisions only when justified by code and product needs

## API rules

- base path is `/api/v1`
- all APIs should stay aligned with [docs/API_Contract_V1.md](C:\Users\Trevor\Documents\GitHub\RentalApp_backend\docs\API_Contract_V1.md)
- do not expose JPA entities directly
- use DTOs for requests and responses

### Current media contract

- listing media is URL-based in listing create/update payloads
- do not document or implement upload endpoints unless that workflow is intentionally introduced

### Current AI contract

- AI enhancement is advisory only
- AI failure must not block listing save/publish workflows
- do not document Qdrant/Ollama integration as present unless code actually wires it

### Current search contract

- public listing browse supports `page`, `size`, and `sort`
- supported sort values are:
  - `PUBLISHED_AT_DESC`
  - `RENT_AMOUNT_ASC`
  - `RENT_AMOUNT_DESC`
  - `CREATED_AT_DESC`
- public browse returns paginated metadata

## Code structure rules

### Controllers

- thin
- routing and validation only
- no business logic

### Services

- own business rules
- own transactions
- interfaces plus implementations where useful

### Repositories

- persistence only
- no buried business rules

### DTOs

- request DTOs use Jakarta validation
- response DTOs are explicit
- no entity leakage

### Entities

- persistent domain state only
- string-backed enums
- prefer `LAZY` relationships

### Dependency injection

- constructor injection only

## Data and persistence rules

- UUID primary keys
- Flyway for schema changes
- add new migrations instead of editing applied ones
- use indexes where query patterns justify them
- avoid over-normalization when a simpler MVP field is better

### Current schema truths

- `agent_recommendations` exists
- `refresh_tokens` exists
- `listing_media` exists and stores media URLs
- `moderation_actions` exists for admin audit logging

### Known schema gaps

- admin-facing moderation history retrieval/view UI does not exist yet
- search indexing can be improved

## Security rules

- JWT-based auth
- BCrypt password hashing
- backend-enforced role and ownership rules
- no trust in client-provided role or ownership

### Agent recommendation rules

- only authenticated users can submit
- self-recommendations are blocked
- admin users cannot submit public recommendations
- one recommendation per author per agent in MVP

## Testing expectations

Every meaningful backend change should preserve:

- controller validation behavior
- service business rules
- authorization and ownership checks
- security expectations

Existing backend tests are part of the quality baseline and should continue passing.

## Frontend integration truths

- the frontend uses a centralized API client in `src/lib/api/client.ts`
- browser session state is shared through `src/lib/auth/sessionStore.ts`
- protected API requests can refresh once on `401` and retry once
- failed refresh clears the stored session and should degrade cleanly to re-authentication

## Documentation discipline

When behavior changes:

- update `docs/MVP_Data_Model.md`
- update `docs/API_Contract_V1.md`
- update `docs/Frontend_Route_Map.md`
- update `plan.md`
- update repo READMEs when setup or current-state claims change

The docs must remain trustworthy.

## Decision heuristics

Prefer:

- precise product naming over overloaded terms
- simpler implementation over speculative architecture
- honest documentation over aspirational documentation
- real rental workflow value over peripheral feature creep
