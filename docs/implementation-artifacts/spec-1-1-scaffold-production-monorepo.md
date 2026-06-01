# Story 1.1 - Scaffold Production Monorepo

Status: Done

## Goal

Create the production-ready monorepo foundation for the Notebook web app, API app, Docker infrastructure, and base project structure.

## Implementation

- Repository contains `apps/web` and `apps/api`.
- Frontend uses Vite React TypeScript.
- Backend uses Spring Boot, Java, Maven, modular domain packages, Flyway migrations, and Docker-oriented configuration.
- Root project files include Docker Compose, environment examples, package scripts, and README setup guidance.
- Backend domain module folders exist for the planned bounded contexts as features are implemented.

## Verification

- `apps/web`: `npm run build`
- `apps/api`: `.\mvnw -DskipTests compile`

