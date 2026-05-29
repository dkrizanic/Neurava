---
title: 'Edit Notes With Autosave Feedback'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 2
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-1-create-and-view-workspace-scoped-notes.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Users can create and list notes, but existing notes are read-only, so they cannot refine captured content or see whether edits are safely persisted.

**Approach:** Add a workspace-scoped note update API and turn note cards into editable fields with debounced autosave and clear saving, saved, and error feedback while preserving typed content on failure.

## Boundaries & Constraints

**Always:** Updates require authentication and active workspace scope; the updated timestamp changes on successful save; UI edits save without a full page reload; failed saves keep local typed content visible.

**Ask First:** Rich text editor modes, conflict resolution, version history, offline queues, and manual revert are later stories.

**Never:** Do not allow updates to notes outside the active workspace, add archiving, or couple notes to workspace/auth persistence internals.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Edit title/body | User changes an existing note | UI shows saving then saved; API returns updated note with newer updatedAt | N/A |
| Save failure | API update rejects | UI shows an error state and leaves typed content in place | Recoverable error message |
| Invalid title | Title becomes blank | API rejects update; UI keeps draft text and shows error | 400 from API |
| Wrong workspace | User attempts update for note outside active workspace | API does not update the note | 404-style not found response |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/notes/domain/Note.java` -- note edit behavior and updated timestamp.
- `apps/api/src/main/java/com/notebook/api/notes/application/NoteService.java` -- workspace-scoped update use case.
- `apps/api/src/main/java/com/notebook/api/notes/infrastructure/web/NoteController.java` -- `PATCH /api/v1/notes/{noteId}` contract.
- `apps/api/src/test/java/com/notebook/api/notes/**` -- update and validation coverage.
- `apps/web/src/features/notes/api/notesApi.ts` -- update note client.
- `apps/web/src/features/notes/components/NotesPage.tsx` -- editable note cards with autosave state.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- add scoped update behavior -- persists title/body edits without leaking workspace data.
- [x] `apps/api/src/test/java/com/notebook/api/notes/**` -- cover update success and validation -- protects autosave API contract.
- [x] `apps/web/src/features/notes/**` -- add debounced autosave editing -- gives saving/saved/error feedback without reload.
- [x] `apps/web/src/features/notes/components/NotesPage.test.tsx` -- test autosave and failed save local preservation -- verifies user trust behavior.

**Acceptance Criteria:**
- Given the user opens an existing note, when they edit title or body, then changes are saved without a full page reload.
- Given autosave succeeds, when the API responds, then the UI shows saved and the note updated timestamp changes.
- Given autosave fails, when the API rejects, then the UI shows a recoverable error without losing typed content.
- Given a note is outside the active workspace, when update is attempted, then the note is not updated.

## Spec Change Log

## Result

- Added workspace-scoped note update behavior through `PATCH /api/v1/notes/{noteId}`.
- Added domain edit behavior that refreshes `updatedAt` after successful saves.
- Converted note cards into editable title/body fields with debounced autosave.
- Added per-note saving, saved, and error feedback while preserving local edits on save failure.
- Added backend unit/functional update coverage and frontend autosave tests.

## Verification

**Commands:**
- `npm run test:api:unit` -- expected: backend unit tests pass.
- `npm run test:api:functional` -- expected: backend functional tests pass with PostgreSQL available.
- `npm --prefix apps/web run test` -- expected: frontend tests pass.
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
