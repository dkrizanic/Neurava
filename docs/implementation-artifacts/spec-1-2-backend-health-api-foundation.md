---
status: done
epic: 1
story: 2
title: Backend Health And API Foundation
createdAt: 2026-05-26
completedAt: 2026-05-26
---

# Story 1.2: Backend Health And API Foundation

## Intent

Expose a small, stable backend API foundation for the frontend and later domain stories: health remains available through Actuator, application APIs live under `/api/v1`, version metadata is available, and validation/runtime errors return sanitized RFC 7807-style problem details.

## Acceptance Criteria

- Given the API app is running, when a client calls `/actuator/health`, then the API returns a healthy status.
- Given the API app is running, when a client calls `/api/v1/system/version`, then the API returns app name, version, environment/profile, and current server time.
- Given the API app receives invalid input on an API endpoint, when validation fails, then the response uses `application/problem+json` with status, title, detail, instance, and field validation details.
- Given an application API path does not exist, when a client calls it, then the response uses problem details and does not expose a stack trace.
- Given local configuration is used, when the app starts, then Spring profile/env configuration works without real OpenAI or Google secrets.
- Given errors occur, when the backend logs or responds, then secrets/tokens are not exposed.

## Implementation Tasks

- Add `shared.infrastructure.config.AppInfoProperties` bound to `app.info`.
- Add `shared.infrastructure.web.ApiPaths` constants for `/api/v1`.
- Add `shared.infrastructure.web.SystemController` with `GET /api/v1/system/version`.
- Add `shared.infrastructure.web.SampleValidationController` as a narrow development contract endpoint for validation behavior under `/api/v1/system/validation-check`.
- Add `shared.infrastructure.errors.GlobalApiExceptionHandler` using Spring `ProblemDetail`.
- Update application config for app info, problem details, and sanitized error response behavior.
- Add MockMvc tests for health, version, validation problem details, and unknown API route problem details.

## Code Map

- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/config/AppInfoProperties.java`
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/web/ApiPaths.java`
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/web/SystemController.java`
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/web/SampleValidationController.java`
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/errors/GlobalApiExceptionHandler.java`
- `apps/api/src/main/resources/application.yml`
- `apps/api/src/main/resources/application-local.yml`
- `apps/api/src/test/java/com/notebook/api/shared/functional/SystemApiFunctionalTest.java`

## Verification

- `docker compose up -d postgres`
- `apps/api/mvnw.cmd test`

## Result

- Added `/api/v1/system/version`.
- Kept `/actuator/health` public and tested.
- Added shared API path constants and app info configuration properties.
- Added sanitized RFC 7807-style problem detail handling for validation failures and missing API resources.
- Added a narrow validation contract endpoint under `/api/v1/system/validation-check` for backend/frontend error-shape tests.
- Added MockMvc contract tests for health, version, validation errors, and missing API routes.
