---
title: 'Manual Note Search And Filters'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 5
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-4-organize-notes-with-tags-favorites-and-links.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Users need to find notes manually before AI search exists.

**Approach:** Add query and metadata filters to the note list API and expose search, tag, favorite, pinned, and archived filters in the Notes UI.

## Boundaries & Constraints

**Always:** Search only returns active workspace notes; filters can be combined; no-result states are explicit.

**Ask First:** Full-text indexes, ranking, saved searches, and AI retrieval belong to later stories.

**Never:** Do not return notes from unavailable workspaces.

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/notes/application/NoteService.java` -- combined filter matching.
- `apps/api/src/main/java/com/notebook/api/notes/infrastructure/web/NoteController.java` -- query parameters for note search.
- `apps/web/src/features/notes/api/notesApi.ts` -- filter-aware note fetch client.
- `apps/web/src/features/notes/components/NotesPage.tsx` -- search/filter controls and no-result state.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- add combined search/filter behavior -- scopes results to active workspace.
- [x] `apps/web/src/features/notes/**` -- add search/filter UI -- lets users find notes without AI.

**Acceptance Criteria:**
- Given a query matches note title or body, when the user searches, then matching notes are shown.
- Given filters are combined, when results are returned, then non-matching notes are hidden.
- Given no notes match, when filters are active, then a no-result state is shown.

## Result

- Added manual query filtering across note title/body.
- Added tag, favorite, pinned, and archived filter combinations.
- Added filter-aware empty state.

## Verification

**Commands:**
- `npm run test:api:unit` -- passed.
- `npm run test:api:functional` -- passed.
- `npm --prefix apps/web run test` -- passed.
- `npm --prefix apps/web run build` -- passed.
