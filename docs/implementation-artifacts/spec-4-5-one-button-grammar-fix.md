# Story 4.5 - One-Button Grammar Fix

Status: Done

## Goal

Let a note writer request a one-button grammar fix from the note editor, compare the current and proposed text, apply the fix through AI Action History, and revert it later.

## Scope

This story adds `fix_note_grammar` as a supported AI action. The preview is deterministic for the MVP and corrects common grammar/spacing issues so the preview/apply/revert workflow can be exercised end to end without introducing live model variability.

## Backend

- Added `fix_note_grammar` to the existing AI action preview and application endpoints.
- Preview accepts note id, title, and current body, then returns current body plus proposed corrected body.
- Apply accepts note id, title, current body, and proposed body.
- Apply updates the note in the active Workspace Context and records an AI Action History update with previous and current note state.
- Revert restores the previous title/body from the history record.
- Revert returns a conflict problem detail if the note no longer exists or previous state cannot be restored.

## Frontend

- Added a `Grammar fix` button to the centered edit-note form.
- The editor shows a compare panel with `Current` and `Proposed` text before applying.
- Applying the fix updates the editor body with the saved note response.
- Editing the body clears the existing preview so stale proposals are not applied.

## Verification

- `apps/api`: `.\mvnw.cmd -q -Dtest=NoteRetrievalIndexFunctionalTest test`
- repo root: `npm --prefix apps/web test -- NotesPage`
