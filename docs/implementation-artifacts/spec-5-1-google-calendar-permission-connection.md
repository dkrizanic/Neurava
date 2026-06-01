# Story 5.1 - Google Calendar Permission Connection

Status: Done

## Goal

Let a signed-in user explicitly connect or disconnect Calendar permission state.

## Implementation

- Added `integration_connection` persistence and workspace-scoped integration APIs.
- Added `/api/v1/integrations/{provider}/connect` and `/disconnect`.
- Added an Integrations page with Calendar permission copy and connect/disconnect controls.
- Calendar connection remains optional and token-free in MVP.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/api`: `.\mvnw '-Dtest=com.notebook.api.shared.unit.ModulithArchitectureUnitTest' test`
- `apps/web`: `npm run build`
- `apps/web`: `npm test -- --run`

