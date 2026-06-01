# Story 6.2 - Business Project Records

Status: Done

## Goal

Let business users create basic project records scoped to the active Business Context.

## Implementation

- Added `project_record` persistence and `/api/v1/projects` APIs.
- Projects include name, description, created timestamp, updated timestamp, and status.
- Added Projects page with project creation and list states.
- Records are always filtered by active Workspace Context.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/web`: `npm run build`

