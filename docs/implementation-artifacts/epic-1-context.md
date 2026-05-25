# Epic 1 Context: Production Foundation And Account Context

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 1 establishes the production foundation for Neurava: a deployable web/API monorepo, a stable backend API contract, secure Google-based authentication, and Personal/Business workspace context boundaries. Later notebook, AI, reminder, integration, and project features all depend on this foundation because every user-facing action must be authenticated, workspace-scoped, observable, and safe for a public web deployment.

## Stories

- Story 1.1: Scaffold Production Monorepo
- Story 1.2: Backend Health And API Foundation
- Story 1.3: Frontend App Shell
- Story 1.4: Google Sign-In And Secure Session
- Story 1.5: Default Personal Context
- Story 1.6: Company Registration Entry And Business Context Availability

## Requirements & Constraints

- The backend must expose health information and stable application APIs that the frontend can depend on.
- Application APIs must live under `/api/v1`.
- API failures must use RFC 7807-style problem details and must not expose stack traces, provider payloads, OAuth tokens, Gmail content, OpenAI sensitive payloads, or secrets.
- Configuration must support local development through Spring profiles and environment variables.
- The authentication model is backend-owned sessions with HttpOnly, Secure, SameSite cookies. OAuth tokens must never be placed in frontend storage.
- Every protected future resource must be workspace-scoped. Personal Context is the default workspace; Business Context is only visible through company ownership, invite, or membership.
- Local infrastructure is Docker-first, with PostgreSQL and pgvector available through Docker Compose.
- The project should remain portfolio-quality: clear structure, production-minded defaults, explicit configuration, and testable contracts.

## Technical Decisions

- Backend is a Spring Boot modular monolith using Spring Modulith and DDD package boundaries.
- Domains stay independent and use `application`, `domain`, and `infrastructure` layers. Cross-domain communication should flow through events.
- Shared backend infrastructure belongs under `shared.infrastructure`, including config, errors, web, security, and events.
- REST is the primary API style. Controllers should be thin and delegate to application use cases as domain behavior appears.
- PostgreSQL is the system of record, with Flyway migrations managing schema changes.
- Spring Actuator is used for operational health.
- API response and error shapes should be stable from the first backend story so the React frontend can build against them.
- Backend tests should cover the API contract where practical, especially response status, content type, and problem detail fields.

## Cross-Story Dependencies

Story 1.2 depends on Story 1.1 because it uses the generated Spring Boot app, Docker PostgreSQL, Flyway, and shared infrastructure folders. Stories 1.4 through 1.6 depend on Story 1.2 because authentication and workspace context APIs need a consistent `/api/v1` base path, problem-details error handling, environment configuration, and health/version contract.
