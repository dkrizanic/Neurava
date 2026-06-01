# Epic 5 Context: Calendar, Gmail, Reminders, And Planning

## Goal

Users can connect optional Google context, create reminders, sync reminder intent to calendar state, and build plans manually or through AI previews.

## Stories

- Story 5.1: Google Calendar Permission Connection
- Story 5.2: Calendar Context In App
- Story 5.3: Smart Gmail Opt-In
- Story 5.4: Source-Aware Gmail Search For AI
- Story 5.5: Manual Reminders With Google Calendar Sync
- Story 5.6: AI-Created Reminders
- Story 5.7: Plans
- Story 5.8: AI-Generated Plan

## Implementation Notes

- MVP stores integration permission state locally and does not persist OAuth tokens.
- Calendar context is represented by deterministic upcoming local context until real Google API access is wired.
- Gmail opt-in state is tracked so assistant behavior can respect permission boundaries.
- Reminders, plans, and AI-created entities are scoped to the active Workspace Context.
- AI reminder and plan creation use the existing preview/apply/action-history pattern.

