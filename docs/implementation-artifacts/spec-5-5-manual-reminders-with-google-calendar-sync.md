# Story 5.5 - Manual Reminders With Google Calendar Sync

Status: Done

## Goal

Let users create reminders and see whether Calendar sync is active, synced, or blocked by missing permission.

## Implementation

- Added `reminder` persistence and `/api/v1/reminders` APIs.
- Added due date, related context, completion state, and calendar sync state.
- Calendar sync is marked `SYNCED` when Calendar is connected, `FAILED` when requested without permission, and `NOT_SYNCED` when disabled.
- Added Reminders page with manual creation, completion, reopening, and sync-state display.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/api`: Modulith architecture test
- `apps/web`: `npm run build`

