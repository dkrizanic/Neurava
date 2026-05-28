---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - docs/planning-artifacts/prds/prd-notebook-2026-05-25/prd.md
workflowType: architecture
project_name: Notebook
user_name: dario
date: 2026-05-25
lastStep: 8
status: complete
completedAt: 2026-05-25
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

Notebook has 24 functional requirements across authentication and workspace contexts, notebook core, AI memory search, AI assistant and AI actions, Google Calendar/Gmail/reminders/planning, business-lite project history, and production quality.

Architecturally, the requirements point to a full-stack AI productivity web app with strong domain separation. The app must support signed-in users, default Personal Context, invite-only Business Context, notes, projects, reminders, plans, AI source references, AI action history, OpenAI-backed assistant workflows, Google Calendar event sync, and opt-in Smart Gmail search.

**Non-Functional Requirements:**

- Production-ready public web quality.
- Responsive and polished UI.
- Accessible core controls and clear loading/error/empty/permission states.
- Privacy-aware Google integrations.
- Source-aware AI responses.
- Revertible AI edits with before/after state retained.
- Workspace Context scoping across navigation, search, assistant, and permissions.

**Scale & Complexity:**

- Primary domain: full-stack AI productivity web application.
- Complexity level: high.
- Estimated architectural components: frontend app, backend API, authentication/security, domain services, persistence, AI orchestration, retrieval/search, Google integrations, reminder sync, audit/action history, and deployment/observability.

### Technical Constraints & Dependencies

- Frontend preference: React.
- Backend preference: Java Spring Boot.
- Database preference: PostgreSQL.
- Architecture preference: DDD.
- LLM provider: OpenAI API.
- Authentication and integrations: Google sign-in, Google Calendar, Gmail.
- MVP reminder sync uses Google Calendar events; Google Tasks is deferred.
- Personal Calendar integration is read-only in MVP.
- Smart Gmail full search is opt-in only.

### Cross-Cutting Concerns Identified

- Authentication and authorization.
- Workspace Context and tenant-like data scoping.
- Google OAuth permission scopes and token security.
- AI action preview, application, history, and revert.
- Source references and traceability for AI answers.
- Privacy boundaries for Calendar and Gmail data.
- Retrieval/search quality across notes, projects, reminders, calendar, and Gmail.
- Domain boundaries that allow Business Context to expand later without overloading the MVP.
- Production UX, accessibility, and failure-state handling.

## Starter Template Evaluation

### Primary Technology Domain

Notebook is a full-stack AI productivity web application. The selected foundation is a two-app monorepo with a React frontend and Spring Boot backend:

```text
notebook/
  apps/
    web/
    api/
```

### Starter Options Considered

**Vite React TypeScript + Spring Initializr**

- Best fit for the stated React, Spring Boot, PostgreSQL, OpenAI, and DDD preferences.
- Keeps generated structure understandable and easy to shape into a portfolio-quality DDD modular monolith.
- Allows frontend and backend decisions to remain explicit.

**JHipster**

- Current and capable full-stack generator with React/Spring support.
- Rejected for MVP because it makes many conventions and product decisions upfront. For this project, custom architecture clarity is more valuable than generated breadth.

### Selected Starter: Vite React TypeScript + Spring Initializr

**Rationale for Selection:**

This foundation gives the app modern frontend tooling while keeping backend architecture flexible enough for DDD, Spring Modulith, Google integrations, OpenAI, and PostgreSQL. It also presents better as a portfolio project because the domain boundaries and architectural decisions will be visible rather than hidden inside a large generator.

**Initialization Commands:**

```bash
npm create vite@latest apps/web -- --template react-ts
```

```bash
curl "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.0.6&javaVersion=21&groupId=com.notebook&artifactId=api&name=api&packageName=com.notebook.api&dependencies=web,security,oauth2-client,data-jpa,postgresql,flyway,validation,actuator,docker-compose,modulith,spring-ai-openai" -o api.zip
```

### Architectural Decisions Provided By Starter

**Language & Runtime:**

- Frontend: React 19, TypeScript, Vite.
- Backend: Java 21, Spring Boot 4, Maven.

**Styling Solution:**

- Starter does not force a styling system. Styling/design-system choice remains an architecture decision.

**Build Tooling:**

- Frontend: Vite build tooling.
- Backend: Maven and Spring Boot plugin.

**Testing Framework:**

- Frontend starter provides the base app but test tooling remains a decision.
- Backend starter provides Spring Boot test foundation.

**Code Organization:**

- Monorepo with `apps/web` and `apps/api`.
- Backend will be organized as a DDD modular monolith using Spring Modulith boundaries.

**Development Experience:**

- Frontend hot reload through Vite.
- Backend Spring Boot local run/dev loop.
- Docker is required for local infrastructure and production-ready packaging.
- PostgreSQL should run through Docker Compose in local development.

**Note:** Project initialization using these commands should be the first implementation story.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**

- Use a modular monolith backend with Spring Boot, Spring Modulith, and DDD package boundaries.
- Use PostgreSQL as the primary database.
- Use pgvector inside PostgreSQL for MVP semantic retrieval instead of adding a separate vector database.
- Use backend-owned OAuth/session flow with HttpOnly secure cookies.
- Use REST APIs with OpenAPI documentation.
- Use React + Vite frontend with React Router and TanStack Query.
- Use Docker Compose for local development infrastructure.

**Important Decisions (Shape Architecture):**

- Use Flyway for database migrations.
- Use Spring Security OAuth2 Client for Google sign-in and Google API authorization.
- Use OpenAI through Spring AI for LLM and embedding integration.
- Use TipTap as the rich text editor foundation, with Markdown-style mode or shortcuts where practical.
- Use Tailwind CSS for UI styling and design tokens.
- Use structured JSON errors for API failures.
- Use application-level audit/action records for AI changes.

**Deferred Decisions (Post-MVP):**

- Separate vector database is deferred until PostgreSQL + pgvector is not enough.
- Google Tasks sync is deferred; MVP syncs reminders to Google Calendar events.
- Full company administration and deep permissions are deferred.
- Kubernetes is deferred; Docker is required, but deployment platform can stay simple for MVP.

### Data Architecture

- Primary database: PostgreSQL.
- Semantic retrieval: pgvector extension, target version 0.8.2.
- Migrations: Flyway.
- ORM/data access: Spring Data JPA for aggregate persistence.
- Domain model: DDD aggregates grouped by bounded context modules.
- Search model: hybrid approach:
  - relational filters for workspace, date, and entity filters;
  - full-text/manual search for note title/body;
  - vector similarity for AI memory search.
- AI Action History stores before/after state for AI edits forever unless related data is deleted by policy or explicit user action.

### Authentication & Security

- Authentication: Google OAuth via Spring Security OAuth2 Client.
- Session model: backend-owned session using HttpOnly, Secure, SameSite cookies.
- Rationale: safer for a React SPA than storing JWTs in browser storage and cleaner for Google token handling.
- Authorization: workspace-scoped access checks on every protected resource.
- Google tokens: stored server-side and encrypted at rest if persisted.
- Smart Gmail: opt-in permission flow only.
- Business Context: visible only through company registration, invite, or membership.

### API & Communication Patterns

- API style: REST first.
- API documentation: OpenAPI via springdoc-openapi for Spring Boot 4.
- Error handling: consistent JSON problem/error shape.
- Validation: backend validation with Bean Validation; frontend validation with Zod where useful.
- Rate limiting: required for authentication, AI, and integration endpoints.
- AI operations: command-style endpoints that produce a preview first, then apply confirmed AI changes.

### Frontend Architecture

- React app under `apps/web`.
- Routing: React Router.
- Server state: TanStack Query.
- Local UI state: React state first; Zustand only for cross-cutting UI state if needed.
- Styling: Tailwind CSS with a small internal design system.
- Icons: lucide-react.
- Editor: TipTap rich text editor, with Markdown-style writing support and examples.
- AI assistant UI: chat/tool panel that can show sources, previews, applied changes, and revert actions.

### Infrastructure & Deployment

- Local development: Docker Compose.
- Services:
  - PostgreSQL with pgvector;
  - Spring Boot API;
  - React web app.
- Backend packaging: Dockerfile for API.
- Frontend packaging: Dockerfile for web.
- Configuration: environment variables plus Spring profiles.
- Observability MVP:
  - Spring Boot Actuator;
  - structured backend logs;
  - clear frontend error states.
- CI/CD: GitHub Actions recommended after scaffold.

### Decision Impact Analysis

**Implementation Sequence:**

1. Scaffold monorepo, frontend, backend, Docker Compose, and database.
2. Establish backend DDD module/package structure and database migrations.
3. Add authentication/session foundation.
4. Implement workspace context and note core.
5. Add AI action preview/apply/history/revert foundation.
6. Add retrieval/search with PostgreSQL full text and pgvector.
7. Add Google Calendar/Gmail integrations.
8. Add production UX and observability polish.

**Cross-Component Dependencies:**

- Workspace Context affects nearly every API, query, AI operation, and UI route.
- AI Action History depends on domain mutation boundaries and persistence design.
- Google integration token storage affects security, data model, and deployment configuration.
- Search quality depends on note/project/reminder schemas plus embedding lifecycle.
- Docker and environment configuration affect local development, CI, and deployment from the first implementation story.

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:**

The project has several areas where future AI agents could make incompatible choices: DDD layering, cross-domain communication, naming conventions, API response/error shapes, date formats, test locations, frontend state ownership, AI action command naming, and logging practices. The following rules are mandatory unless the architecture document is explicitly updated.

### DDD And Domain Module Patterns

**Domain Independence:**

- Domains are independent modules.
- One domain must not directly reach into another domain's repositories, entities, or internal services.
- Cross-domain communication happens through domain/application events.
- Shared code must be genuinely generic and placed in shared/common packages, not hidden domain coupling.

**Required Domain Layers:**

Every backend domain module must use three layers:

```text
<domain>/
  application/
  domain/
  infrastructure/
```

- `application`: use cases, commands, queries, application services, transaction boundaries, event handlers that orchestrate use cases.
- `domain`: aggregates, entities, value objects, domain services, domain events, repository interfaces, invariants.
- `infrastructure`: JPA entities/mappers, Spring Data repositories, external clients, persistence adapters, integration adapters, configuration specific to that domain.

**Allowed Dependency Direction:**

```text
infrastructure -> application -> domain
```

- `domain` depends on no application or infrastructure code.
- `application` may depend on `domain`.
- `infrastructure` may depend on `application` and `domain`.
- Controllers may call application use cases but must not contain domain logic.

**Event Communication:**

- Cross-domain workflows publish events instead of directly invoking another domain's internals.
- Event names use past-tense business facts, for example `NoteCreated`, `AiActionApplied`, `ReminderScheduled`.
- Event payloads contain stable IDs and necessary facts, not full mutable aggregate objects.
- Event handlers must be idempotent where possible.

### Naming Patterns

**Database Naming Conventions:**

- Tables: plural `snake_case`, for example `notes`, `workspace_memberships`, `ai_action_records`.
- Columns: `snake_case`, for example `workspace_id`, `created_at`, `google_event_id`.
- Primary keys: `id` with UUID values.
- Foreign keys: `<referenced_table_singular>_id`, for example `workspace_id`, `note_id`.
- Indexes: `idx_<table>_<columns>`, for example `idx_notes_workspace_id_updated_at`.
- Unique constraints: `uk_<table>_<columns>`.

**API Naming Conventions:**

- Base path: `/api/v1`.
- Resource names: plural nouns, for example `/api/v1/notes`, `/api/v1/reminders`.
- Route parameters: path style `{id}` in Spring controllers.
- Query parameters and JSON fields: `camelCase`.
- Custom headers: avoid unless required; if needed use clear `X-Notebook-*` names.

**Code Naming Conventions:**

- Java classes: `PascalCase`.
- Java methods and fields: `camelCase`.
- Java packages: lowercase domain names.
- React components: `PascalCase.tsx`.
- React hooks: `useThing.ts`.
- TypeScript variables/functions: `camelCase`.
- TypeScript types/interfaces: `PascalCase`.

### Structure Patterns

**Backend Project Organization:**

- Backend code lives under `apps/api`.
- Domain modules live under `com.notebook.api.<domain>`.
- Each domain follows `application`, `domain`, `infrastructure`.
- Cross-cutting shared code lives under `com.notebook.api.shared`.
- Spring web controllers should be thin and delegate to application use cases.
- Repository interfaces live in the domain layer; Spring Data/JPA implementations live in infrastructure.

**Frontend Project Organization:**

- Frontend code lives under `apps/web`.
- Organize by feature first, then shared UI.
- Shared design-system components live under `src/shared/ui`.
- API clients live under `src/shared/api` or feature-level API folders when domain-specific.
- Route-level screens live under `src/features/<feature>/pages` or equivalent feature folder.
- Tests are colocated with frontend components as `*.test.tsx` unless integration-level.

**Configuration And Assets:**

- Environment examples must be committed as `.env.example`.
- Secrets are never committed.
- Static assets live under the frontend's standard public/assets locations.
- Docker configuration lives at repository root or in each app folder when app-specific.

### Format Patterns

**API Response Formats:**

- Successful single-resource responses return the resource object directly.
- Collection responses use:

```json
{
  "items": [],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

- Command responses that create async or previewable work include IDs and state, for example:

```json
{
  "actionId": "uuid",
  "status": "previewReady"
}
```

**Error Response Formats:**

- Use RFC 7807-style problem details.
- Include `type`, `title`, `status`, `detail`, `instance`, and optional `errors` for validation details.
- Do not leak stack traces, tokens, provider payloads, or secrets.

**Data Exchange Formats:**

- JSON field names use `camelCase`.
- Dates/times use ISO 8601 UTC strings.
- IDs are UUID strings.
- Nullable fields are explicit; avoid overloading empty strings as null.

### Communication Patterns

**Event System Patterns:**

- Domain/application events use `PascalCase` names and past-tense business facts.
- Event payloads include `eventId`, `occurredAt`, and stable domain IDs.
- Events are internal backend integration boundaries first; external message broker can be deferred.
- If persisted event/outbox support is introduced, use a single consistent outbox table and publisher.

**State Management Patterns:**

- Server state in the frontend uses TanStack Query.
- Local component state uses React state.
- Cross-cutting UI state may use Zustand only when props/context become awkward.
- Do not duplicate server state into global client stores.
- Mutations invalidate or update the relevant TanStack Query keys.

### Process Patterns

**Error Handling Patterns:**

- Backend validation errors map to problem details with field-level `errors`.
- Frontend displays user-readable messages and can expose technical detail in development.
- Integration failures from Google/OpenAI should be recoverable where possible and never lose user data.
- AI answers must show source references when using retrieved content.

**Loading State Patterns:**

- Route-level loading states for screen transitions.
- Component-level loading states for contained operations.
- AI operations show multi-state progress: preparing context, generating preview, applying change, completed.
- Empty states are required for notes, search results, reminders, projects, and integrations.

**AI Action Patterns:**

- AI mutations use preview/apply/revert flow.
- Preview endpoints do not persist final user-visible changes.
- Apply endpoints persist the change and create AI Action History.
- Revert endpoints restore previous state from AI Action History.
- AI Action History records changed entities, previous state, current state, summary, timestamp, and user/context.

### Enforcement Guidelines

**All AI Agents MUST:**

- Preserve DDD module independence and the three-layer domain structure.
- Communicate between domains through events, not direct domain-internal calls.
- Use `camelCase` JSON and `snake_case` database naming.
- Scope protected data by Workspace Context.
- Keep controllers thin and domain logic out of infrastructure.
- Use problem details for API errors.
- Use ISO 8601 UTC strings for dates/times.
- Avoid logging secrets, OAuth tokens, Gmail content, or OpenAI raw sensitive payloads.

**Pattern Enforcement:**

- New code should be reviewed against this architecture document.
- Pattern violations should be fixed before adding new feature work on top.
- If a pattern becomes wrong, update architecture first, then implementation.

### Pattern Examples

**Good Examples:**

- `com.notebook.api.notes.domain.Note`
- `com.notebook.api.notes.application.CreateNoteUseCase`
- `com.notebook.api.notes.infrastructure.JpaNoteRepository`
- `/api/v1/notes/{id}`
- `idx_notes_workspace_id_updated_at`
- `AiActionApplied` event with IDs and timestamps.

**Anti-Patterns:**

- A reminders service directly modifying note internals.
- A controller containing business rules for AI action revert.
- Storing Google OAuth tokens in frontend local storage.
- Returning stack traces in API responses.
- Creating a global frontend store that duplicates all note server state.

## Project Structure & Boundaries

### Complete Project Directory Structure

```text
notebook/
  README.md
  .gitignore
  .env.example
  docker-compose.yml
  package.json
  docs/
    planning-artifacts/
      architecture.md
      prds/
        prd-notebook-2026-05-25/
          prd.md
          .decision-log.md
    implementation-artifacts/
  apps/
    web/
      Dockerfile
      package.json
      vite.config.ts
      tsconfig.json
      index.html
      public/
      src/
        main.tsx
        app/
          App.tsx
          router.tsx
          providers/
            QueryProvider.tsx
            AuthProvider.tsx
          styles/
            globals.css
        features/
          auth/
            api/
            components/
            pages/
          workspace/
            api/
            components/
            hooks/
          notes/
            api/
            components/
            editor/
            pages/
            types.ts
          assistant/
            api/
            components/
            hooks/
            types.ts
          search/
            api/
            components/
            pages/
          reminders/
            api/
            components/
            pages/
          plans/
            api/
            components/
            pages/
          projects/
            api/
            components/
            pages/
          integrations/
            api/
            components/
            pages/
        shared/
          api/
            httpClient.ts
            problemDetails.ts
          ui/
            button/
            dialog/
            form/
            input/
            layout/
            loading/
          lib/
            dates.ts
            ids.ts
          types/
    api/
      Dockerfile
      pom.xml
      src/
        main/
          java/
            com/notebook/api/
              NotebookApiApplication.java
              shared/
                application/
                domain/
                infrastructure/
                  web/
                  security/
                  events/
                  errors/
                  config/
              auth/
                application/
                domain/
                infrastructure/
              workspace/
                application/
                domain/
                infrastructure/
              notes/
                application/
                domain/
                infrastructure/
              ai/
                application/
                domain/
                infrastructure/
              search/
                application/
                domain/
                infrastructure/
              reminders/
                application/
                domain/
                infrastructure/
              plans/
                application/
                domain/
                infrastructure/
              projects/
                application/
                domain/
                infrastructure/
              integrations/
                application/
                domain/
                infrastructure/
          resources/
            application.yml
            application-local.yml
            db/
              migration/
                V1__init_extensions.sql
                V2__auth_workspace.sql
                V3__notes.sql
                V4__ai_actions.sql
                V5__reminders_plans_projects.sql
                V6__integrations.sql
        test/
          java/
            com/notebook/api/
              auth/
              workspace/
              notes/
              ai/
              search/
              reminders/
              plans/
              projects/
              integrations/
  .github/
    workflows/
      ci.yml
```

### Architectural Boundaries

**API Boundaries:**

- All external app APIs live under `/api/v1`.
- Controllers are infrastructure adapters and call application use cases.
- Controllers must not contain domain logic.
- API resources are workspace-scoped unless explicitly public.
- AI preview/apply/revert operations use command-style endpoints.

**Component Boundaries:**

- Frontend features own their screens, API hooks, local feature components, and feature-specific types.
- Shared UI components cannot import feature modules.
- Feature modules may import `shared/*`.
- Server state belongs in TanStack Query; avoid global duplication.

**Service Boundaries:**

- Backend domains are independent DDD modules.
- Each domain owns its aggregate rules and persistence adapters.
- Cross-domain communication uses events.
- Direct repository/service access across domains is forbidden.

**Data Boundaries:**

- PostgreSQL is the system of record.
- Each domain owns its schema tables conceptually, even inside one database.
- Workspace Context scoping is required for protected user data.
- pgvector embeddings support retrieval but do not replace relational ownership.
- Google/OpenAI provider payloads are integration data and must not leak into domain entities.

### Requirements To Structure Mapping

**Authentication And Workspace Contexts (FR-1 to FR-4):**

- Backend: `auth`, `workspace`, `shared.infrastructure.security`.
- Frontend: `features/auth`, `features/workspace`.
- Database: `V2__auth_workspace.sql`.

**Notebook Core (FR-5 to FR-8):**

- Backend: `notes`.
- Frontend: `features/notes`, `features/notes/editor`.
- Database: `V3__notes.sql`.

**AI Memory Search (FR-9 to FR-11):**

- Backend: `search`, `ai`, domain events from source domains.
- Frontend: `features/search`, `features/assistant`.
- Database: note/search tables plus pgvector extension in `V1__init_extensions.sql`.

**AI Assistant And AI Actions (FR-12 to FR-15):**

- Backend: `ai`.
- Frontend: `features/assistant`.
- Database: `V4__ai_actions.sql`.

**Google Calendar, Gmail, Reminders, And Planning (FR-16 to FR-19):**

- Backend: `integrations`, `reminders`, `plans`.
- Frontend: `features/integrations`, `features/reminders`, `features/plans`.
- Database: `V5__reminders_plans_projects.sql`, `V6__integrations.sql`.

**Business-Lite Project History (FR-20 to FR-21):**

- Backend: `projects`, `workspace`.
- Frontend: `features/projects`, `features/workspace`.
- Database: `V5__reminders_plans_projects.sql`.

**Production Quality Layer (FR-22 to FR-24):**

- Backend: `shared.infrastructure.errors`, `shared.infrastructure.config`, Spring Actuator.
- Frontend: `shared/ui`, route error/loading states, integration permission states.
- Infrastructure: Docker, CI, environment files, structured logs.

### Integration Points

**Internal Communication:**

- Frontend communicates with backend through REST APIs.
- Backend domain-to-domain communication uses events.
- Application use cases are transaction boundaries.
- AI action apply/revert use cases publish events after state changes.

**External Integrations:**

- Google OAuth: `auth` and `integrations`.
- Google Calendar: `integrations` with `reminders`.
- Gmail/Smart Gmail: `integrations` with `search` and `ai`.
- OpenAI: `ai` and `search` through Spring AI.

**Data Flow:**

1. User interacts with React feature UI.
2. Feature API hook calls `/api/v1`.
3. Controller validates request and delegates to an application use case.
4. Application use case loads aggregates through domain repository interfaces.
5. Infrastructure repositories persist changes through JPA/PostgreSQL.
6. Domain/application events notify other modules.
7. Frontend invalidates or updates TanStack Query caches.

### File Organization Patterns

**Configuration Files:**

- Root `.env.example` documents shared environment variables.
- `apps/api/src/main/resources/application*.yml` holds Spring configuration.
- `apps/web/.env.example` can be added if frontend-specific environment variables grow.
- Docker Compose is rooted at repository root.

**Source Organization:**

- Backend source follows DDD domain modules.
- Frontend source follows feature-first modules plus shared UI/API utilities.
- Shared backend code is allowed only for cross-cutting primitives, not domain shortcuts.

**Test Organization:**

- Backend tests live under `apps/api/src/test/java/com/notebook/api/<domain>`.
- Frontend component/unit tests are colocated as `*.test.tsx`.
- E2E tests can be added under a root `tests/e2e` or `apps/web/e2e` once Playwright is introduced.

**Asset Organization:**

- Public frontend assets live in `apps/web/public`.
- User uploaded files/attachments are out of MVP scope.

### Development Workflow Integration

**Development Server Structure:**

- Docker Compose starts PostgreSQL with pgvector and local dependencies.
- API runs from `apps/api`.
- Web runs from `apps/web`.

**Build Process Structure:**

- Frontend builds through Vite.
- Backend builds through Maven.
- Dockerfiles package each app independently.

**Deployment Structure:**

- API and web can deploy as separate containers.
- PostgreSQL is an external managed service or Docker-backed local service.
- Environment variables configure OpenAI, Google OAuth, database, cookie/session, and allowed origins.

## Architecture Validation Results

### Coherence Validation

**Decision Compatibility:**

The architecture is coherent. React/Vite, Spring Boot, PostgreSQL/pgvector, OpenAI, Google OAuth, and Docker fit together for a full-stack AI productivity web app. The modular monolith approach keeps the MVP deployable as one backend while preserving domain boundaries for future growth.

**Pattern Consistency:**

Implementation patterns support the architectural decisions. DDD layering, event-based domain communication, REST conventions, problem-details errors, frontend feature organization, and AI preview/apply/revert flows are consistent with the selected stack.

**Structure Alignment:**

The project structure supports all major architectural decisions. Backend domains have the required `application`, `domain`, and `infrastructure` layers. Frontend features map cleanly to PRD capabilities. Integration points for Google, OpenAI, PostgreSQL, pgvector, and Docker are placed in explicit modules.

### Requirements Coverage Validation

**Feature Coverage:**

All PRD feature groups are architecturally supported:

- Authentication and workspace contexts: `auth`, `workspace`, shared security.
- Notebook core: `notes`.
- AI memory search: `search`, `ai`, pgvector.
- AI assistant and AI actions: `ai`.
- Google Calendar, Gmail, reminders, and planning: `integrations`, `reminders`, `plans`.
- Business-lite project history: `projects`, `workspace`.
- Production quality: shared errors/config, frontend shared UI, Docker, structured logs.

**Functional Requirements Coverage:**

- FR-1 to FR-4 are covered by auth, workspace, and security boundaries.
- FR-5 to FR-8 are covered by notes and frontend editor structure.
- FR-9 to FR-11 are covered by search, AI, source references, and pgvector.
- FR-12 to FR-15 are covered by assistant, AI actions, preview/apply/revert, and Spring AI/OpenAI.
- FR-16 to FR-19 are covered by integrations, reminders, plans, and Google Calendar event sync.
- FR-20 to FR-21 are covered by projects and workspace boundaries.
- FR-22 to FR-24 are covered by frontend shared UI, error/loading states, and integration permission controls.

**Non-Functional Requirements Coverage:**

- Security: backend-owned session cookies, Spring Security, workspace-scoped access checks, server-side Google token handling.
- Privacy: opt-in Gmail, permission controls, no sensitive logs.
- Reliability: Flyway migrations, Docker local infrastructure, structured errors.
- UX quality: frontend feature structure, shared UI, loading/empty/error states.
- AI trust: source references, preview before AI edits, AI Action History and revert.

### Implementation Readiness Validation

**Decision Completeness:**

Critical technology decisions, versions, and rationale are documented. Minor implementation choices remain intentionally deferred to implementation stories where they do not block scaffolding.

**Structure Completeness:**

The architecture defines a concrete monorepo structure, backend domain modules, frontend feature modules, migration layout, Docker locations, and CI location.

**Pattern Completeness:**

Naming, structure, API format, event communication, state management, error handling, loading states, AI actions, and enforcement guidelines are documented with examples and anti-patterns.

### Gap Analysis Results

**Critical Gaps:**

- None.

**Important Gaps:**

- Exact deployment host is not chosen.
- Exact token encryption mechanism for persisted Google tokens is not chosen.
- Exact rate limiting implementation is not chosen.
- Exact OpenAPI library/version for Spring Boot 4 should be confirmed during backend setup.
- CI/CD workflow details are not yet specified beyond GitHub Actions recommendation.

**Nice-To-Have Gaps:**

- Add diagrams after scaffold if useful.
- Add ADR files for major decisions if the project grows.
- Add E2E test structure when UI flows stabilize.

### Validation Issues Addressed

No critical validation issues were found. Minor gaps are implementation-level choices and do not block initial scaffold or architecture handoff.

### Architecture Completeness Checklist

**Requirements Analysis**

- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified
- [x] Cross-cutting concerns mapped

**Architectural Decisions**

- [x] Critical decisions documented with versions
- [x] Technology stack fully specified
- [x] Integration patterns defined
- [x] Performance considerations addressed

**Implementation Patterns**

- [x] Naming conventions established
- [x] Structure patterns defined
- [x] Communication patterns specified
- [x] Process patterns documented

**Project Structure**

- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** High

**Key Strengths:**

- Clear DDD modular-monolith structure.
- Explicit cross-domain event communication rules.
- Strong AI action safety model with preview, history, and revert.
- Practical MVP retrieval approach using PostgreSQL plus pgvector.
- Clear separation of Personal and Business Context concerns.
- Docker-first development and packaging foundation.

**Areas For Future Enhancement:**

- Deployment platform decision.
- CI/CD hardening.
- Full backup/restore strategy.
- Deeper business permissions model.
- Optional external event broker/outbox if cross-domain workflows grow.

### Implementation Handoff

**AI Agent Guidelines:**

- Follow all architectural decisions exactly as documented.
- Use implementation patterns consistently across all components.
- Respect project structure and DDD boundaries.
- Communicate across domains through events.
- Refer to this document for all architectural questions.

**First Implementation Priority:**

Scaffold the monorepo, frontend, backend, Docker Compose, PostgreSQL/pgvector, base configuration, and empty DDD domain module structure.
