# Story 4.4 - Revert AI Actions

Status: Done

## Goal

Let users revert supported AI-applied changes from AI Action History while preserving workspace scoping and giving a clear explanation when the related data can no longer be restored.

## Scope

This story implements revert for the AI action type currently supported by the product: `create_note`. Reverting a `create_note` action restores the previous state by deleting the AI-created note and removing its retrieval index row. Other AI action types return a conflict response until those action types exist.

## Backend

- Added `reverted_at` and `revert_summary` columns to `ai_action_history`.
- Extended `AiActionHistoryRecord` and `AiActionHistorySummary` with revert state.
- Added `POST /api/v1/ai/action-history/{historyId}/revert`.
- Revert lookup is constrained by active Workspace Context.
- Reverting an AI-created note deletes the note through `NoteService`.
- Note deletion publishes `NoteDeletedEvent`; the retrieval index listener removes the note from `note_retrieval_index`.
- Reverting an already reverted record returns the existing reverted summary.
- Missing related notes return a problem-details conflict explaining that the note no longer exists.

## Frontend

- AI Action History now shows a `Revert` button for unreverted `create_note` history rows.
- Successful revert updates the visible history row and posts a status message in the assistant chat.
- Failed revert posts a user-visible error message explaining that related data may no longer exist.
- Reverted history rows display the revert summary instead of another revert button.

## Verification

- `apps/api`: `.\mvnw.cmd -q -Dtest=NoteRetrievalIndexFunctionalTest test`
- `apps/api`: `.\mvnw.cmd -q test`
- repo root: `npm --prefix apps/web test -- --run`
- repo root: `npm --prefix apps/web run build`
