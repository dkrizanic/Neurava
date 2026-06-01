# Story 6.3 - Project Notes

Status: Done

## Goal

Let notes attach to projects through existing workspace-scoped links.

## Implementation

- Notes already support `linkedResources` metadata and workspace scoping.
- Project summaries treat note linked resources containing project id or project name as project source notes.
- Linked notes remain governed by the existing notes filters, dates, tags, and workspace boundaries.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/web`: `npm run build`

