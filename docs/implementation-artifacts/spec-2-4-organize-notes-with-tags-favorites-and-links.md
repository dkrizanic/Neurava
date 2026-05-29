---
title: 'Organize Notes With Tags Favorites And Links'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 4
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-3-archive-and-restore-notes.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Users can write notes but cannot yet organize or highlight important context.

**Approach:** Add note metadata for tags, favorite, pinned, and linked resource references, expose a scoped organization API, and make metadata editable and filterable in the Notes UI.

## Boundaries & Constraints

**Always:** Organization metadata is workspace scoped; favorite/pinned filters combine with other note filters; linked resource references are stored as safe metadata until reminders, plans, calendar events, and projects have full modules.

**Ask First:** Dedicated normalized link tables per future module and link pickers are deferred until those modules exist.

**Never:** Do not link across unavailable workspaces or import future module persistence models into notes.

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V7__note_metadata.sql` -- adds tags, favorite, pinned, and linked resource metadata.
- `apps/api/src/main/java/com/notebook/api/notes/**` -- organization use case and filters.
- `apps/web/src/features/notes/components/NotesPage.tsx` -- metadata controls and filters.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V7__note_metadata.sql` -- add organization fields -- persists note metadata.
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- add organization endpoint -- updates metadata in active workspace.
- [x] `apps/web/src/features/notes/**` -- add metadata controls -- saves and filters organization state.

**Acceptance Criteria:**
- Given a user edits tags, favorite, pinned, or links, when save succeeds, then metadata is stored and visible.
- Given filters are set, when notes are listed, then matching workspace notes are returned.

## Result

- Added note metadata persistence and organization endpoint.
- Added tags, favorites, pinned state, linked resources, and matching filters to the Notes UI.

## Verification

**Commands:**
- `npm run test:api:unit` -- passed.
- `npm run test:api:functional` -- passed.
- `npm --prefix apps/web run test` -- passed.
- `npm --prefix apps/web run build` -- passed.
