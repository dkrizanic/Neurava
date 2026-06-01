# Story 5.2 - Calendar Context In App

Status: Done

## Goal

Show upcoming calendar-aware planning context when Calendar is connected.

## Implementation

- Added `/api/v1/integrations/calendar/events`.
- Calendar events are hidden when Calendar is not connected.
- Integrations page shows upcoming local calendar context after connection.
- Reminder sync state can reference Calendar availability.

## Verification

- `apps/web`: `npm run build`
- `apps/web`: `npm test -- --run`

