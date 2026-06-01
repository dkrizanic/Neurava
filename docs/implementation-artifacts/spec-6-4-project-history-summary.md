# Story 6.4 - Project History Summary

Status: Done

## Goal

Let users summarize project history from linked notes and source context.

## Implementation

- Added `/api/v1/projects/{projectId}/summary`.
- Summary returns timeline, decisions, unresolved items, next actions, and source notes.
- Projects page exposes a Summarize action for each project.
- Summaries respect active Workspace Context permissions.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/web`: `npm run build`

