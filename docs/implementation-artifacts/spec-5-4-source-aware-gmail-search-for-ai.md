# Story 5.4 - Source-Aware Gmail Search For AI

Status: Done

## Goal

Prepare Gmail as an optional assistant source while preserving explicit opt-in and source-reference behavior.

## Implementation

- Smart Gmail permission state is tracked independently from Calendar.
- Assistant source-reference types already include `gmailThread`.
- MVP does not perform live Gmail search until OAuth token handling is added, but the UI/API permission boundary is in place.

## Verification

- `apps/web`: `npm run build`
- `apps/api`: `.\mvnw -DskipTests compile`

