# RentalApp Backend

Spring Boot backend for the Rental House Hunting Platform MVP.

## Current status

Phase 0 and Module 1 scaffold:

- Maven project foundation
- shared API response and exception handling
- JWT security scaffold
- auth module with register, login, and me endpoints
- Flyway auth migration
- Docker Compose for local Postgres
## Configuration

The backend now uses Spring profiles with `local` as the default profile.

- `application.yml` contains shared defaults and environment-variable based production config
- `application-local.yml` contains safe local development defaults
- `application-prod.yml` is the production profile baseline

For local setup, create a `.env` from [.env.example](./.env.example) or export the variables in your shell before starting the app.
