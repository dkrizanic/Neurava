# Story 5.3 - Smart Gmail Opt-In

Status: Done

## Goal

Let a signed-in user explicitly opt into Smart Gmail before email context can be used.

## Implementation

- Reused the integration permission model for `GMAIL`.
- Integrations page shows Smart Gmail permission copy and connect/disconnect controls.
- Gmail is never enabled silently and no Gmail content is logged or stored in MVP.

## Verification

- `apps/api`: `.\mvnw -DskipTests compile`
- `apps/web`: `npm run build`

