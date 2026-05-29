---
title: 'Selectable Professional Editor Modes'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 6
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-5-manual-note-search-and-filters.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Users need a writing style that fits different work modes instead of one generic note editor.

**Approach:** Add selectable note editor modes with labeled examples for rich text, Markdown-style notes, and journal-style notes, and persist the selected mode with each note.

## Boundaries & Constraints

**Always:** Mode controls are keyboard accessible and labeled; selected mode is persisted; mode examples are visible in the option labels.

**Ask First:** Full rich text rendering, Markdown preview, per-account default preferences, and editor plugins are later stories.

**Never:** Do not introduce a heavy editor framework before the professional editor mode story needs it.

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/notes/domain/EditorMode.java` -- supported note modes.
- `apps/api/src/main/resources/db/migration/V7__note_metadata.sql` -- persisted `editor_mode`.
- `apps/api/src/main/java/com/notebook/api/notes/**` -- mode save/response shape.
- `apps/web/src/features/notes/components/NotesPage.tsx` -- accessible mode selector with examples.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/notes/domain/EditorMode.java` -- add mode enum -- constrains professional modes.
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- persist and return editor mode -- remembers selected mode.
- [x] `apps/web/src/features/notes/components/NotesPage.tsx` -- add labeled selector -- exposes mode examples.

**Acceptance Criteria:**
- Given a user opens a note, when mode options are shown, then rich text, Markdown, and journal examples are visible.
- Given a user selects a mode, when save succeeds, then the selected mode is remembered.
- Given keyboard navigation, when focusing editor controls, then labels and controls are accessible.

## Result

- Added persisted editor mode metadata.
- Added selectable Rich Text, Markdown, and Journal modes with examples in the Notes UI.

## Verification

**Commands:**
- `npm run test:api:unit` -- passed.
- `npm run test:api:functional` -- passed.
- `npm --prefix apps/web run test` -- passed.
- `npm --prefix apps/web run build` -- passed.
