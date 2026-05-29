---
title: 'Archive And Restore Notes'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 3
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-2-edit-notes-with-autosave-feedback.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Users need to clean up their active note list without permanently deleting workspace-scoped notes.

**Approach:** Add archived state to notes, expose archive/restore API actions, and let the Notes UI switch between active and archived filtered views.

## Boundaries & Constraints

**Always:** Archive/restore is scoped to the active workspace; archived notes disappear from the default active list; archived notes remain retrievable through the archived filter.

**Ask First:** Permanent deletion, bulk archive, retention policy, and undo history are later decisions.

**Never:** Do not return archived notes in the default active list or update notes outside the active workspace.

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V7__note_metadata.sql` -- adds `archived_at` and archive-aware index.
- `apps/api/src/main/java/com/notebook/api/notes/domain/Note.java` -- archive/restore domain behavior.
- `apps/api/src/main/java/com/notebook/api/notes/application/NoteService.java` -- scoped archive/restore use cases and archived filtering.
- `apps/api/src/main/java/com/notebook/api/notes/infrastructure/web/NoteController.java` -- archive/restore endpoints.
- `apps/web/src/features/notes/components/NotesPage.tsx` -- archived filter and Archive/Restore controls.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V7__note_metadata.sql` -- add archived state -- preserves notes outside the active list.
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- add archive/restore actions -- enforces workspace scoping.
- [x] `apps/web/src/features/notes/**` -- add archive UI and archived filter -- makes archived notes retrievable.

**Acceptance Criteria:**
- Given a user archives a note, when the active list reloads, then the note is removed from active notes.
- Given archived mode is enabled, when archived notes exist, then archived workspace notes are shown.
- Given a user restores a note, when active mode is enabled, then the note is available again.

## Result

- Added archive/restore API actions and UI controls.
- Added archived filtering to active workspace note lists.
- Added unit, functional, and frontend coverage through the Epic 2 note test suites.

## Verification

**Commands:**
- `npm run test:api:unit` -- passed.
- `npm run test:api:functional` -- passed.
- `npm --prefix apps/web run test` -- passed.
- `npm --prefix apps/web run build` -- passed.
