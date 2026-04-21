# RentalApp MVP Demo Guide

This guide is for local MVP demos. It describes the current working app honestly: frontend, backend, and Postgres run through Docker Compose; AI is lightweight heuristic assistance; media uploads are stored by the backend on local filesystem storage.

## Start the App

From the backend repo:

```powershell
docker compose up -d
```

Open:

```text
http://localhost:3000
```

Useful services:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- Postgres: `localhost:5433`

Stop the stack:

```powershell
docker compose down
```

## Recommended Demo Flow

1. Register or sign in as an `AGENT` or `LANDLORD`.
2. Complete the profile with city, service areas, company, fee structure, and bio.
3. Create a listing from `/my-listings/new`.
4. Upload one or more listing images.
5. Use the AI description helper to improve the listing copy.
6. Save the listing and publish it.
7. Browse public listings from `/listings`.
8. Try the natural-language search helper, for example: `2 bedroom in Kilimani under 50k with parking`.
9. Sign in as a renter, save a listing, and submit an inquiry.
10. Open the agent public profile and submit a recommendation.
11. Sign in as an admin and moderate the recommendation from `/admin/recommendations`.
12. Review recent trust actions on the admin overview page.

## Current AI Scope

Implemented now:

- listing description enhancement
- natural-language search interpretation into structured filters

Not implemented yet:

- Spring AI provider runtime
- Ollama container/runtime integration
- Qdrant/vector search
- semantic ranking

## Current Media Scope

Implemented now:

- authenticated image upload for listing creators
- backend local filesystem storage
- public `/media/**` serving
- external media URLs as a secondary fallback

Not implemented yet:

- cloud object storage
- CDN delivery
- image resizing/transcoding
- orphaned upload cleanup

## Demo Data Note

There is no global Flyway seed data for demo users/listings. That is intentional: seed rows should not be inserted into every environment by default. For demos, create users and listings through the UI so the flow remains realistic.
