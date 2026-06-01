---
title: 'Apply AI Actions'
type: 'feature'
created: '2026-06-01'
status: 'done'
epic: 4
story: 2
context:
  - '{project-root}/docs/implementation-artifacts/epic-4-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-4-1-ai-change-preview.md'
---

## Intent

**Problem:** AI change previews can propose a note draft, but approved previews cannot yet be applied through a controlled AI action path.

**Approach:** Add a typed, authenticated apply endpoint for the first supported data-changing action, `create_note`, and reconnect the Assistant UI so create-note requests preview first, then persist only when the user clicks Apply.

## Boundaries & Constraints

**Always:** Require an explicit supported action name, scope the applied change to the active Workspace Context, reuse the notes application service for persistence, keep unsupported actions refused, and show what changed after apply.

**Ask First:** Durable AI Action History, revert, reminders, plans, project summaries, grammar fixes, and autonomous multi-step tools remain later stories.

**Never:** Apply arbitrary client-provided tool names, bypass notes module boundaries, create data during preview, or expose provider/internal payloads.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Apply create-note preview | Signed-in user applies a `create_note` preview | API persists the note in active workspace and returns the created note | N/A |
| UI apply | Preview card is visible | Apply button saves the note and shows a saved confirmation | Recoverable error message on failure |
| Cancel preview | Preview card is visible | Cancel removes the preview and does not apply | N/A |
| Unsupported action | Client posts an unsupported action | No data changes occur | Sanitized problem details |
| Module boundary | AI applies a note | AI depends on notes application API only | Modulith verification passes |

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/AssistantActionApplicationService.java` -- applies supported AI action requests.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantActionApplicationController.java` -- exposes `/api/v1/ai/action-applications`.
- `apps/api/src/main/java/com/notebook/api/notes/application/NoteService.java` -- provides note creation with tags without exposing notes domain internals.
- `apps/web/src/features/assistant/api/assistantApi.ts` -- calls the apply endpoint.
- `apps/web/src/features/assistant/components/AssistantPage.tsx` -- shows preview cards with Apply and Cancel.

## Tasks & Acceptance

**Execution:**
- [x] Add typed backend apply request/response records and unsupported-action handling.
- [x] Add `create_note` application service behavior that creates a workspace-scoped note.
- [x] Add API functional coverage for successful apply and unsupported apply actions.
- [x] Add frontend API helper and tests for applying note previews.
- [x] Update Assistant UI to preview create-note prompts, apply approved previews, cancel previews, and show saved confirmation.

**Acceptance Criteria:**
- Given an AI note preview exists, when the user applies it, then a note is persisted in the active workspace.
- Given the note is applied, when the UI updates, then it shows what changed.
- Given the user cancels a preview, when the preview is removed, then no note is applied.
- Given an unsupported apply action is requested, when the API receives it, then it returns sanitized problem details and performs no data change.

## Verification

**Commands:**
- `.\mvnw.cmd -q test` from `apps/api` -- passed: 58 tests.
- `npm --prefix apps/web test -- --run` -- passed: 8 files, 31 tests.
- `npm --prefix apps/web run build` -- passed.

## Result

Implemented Story 4.2 for the currently supported AI action. The assistant now previews a create-note request, lets the user apply or cancel it, persists approved note creations through a typed AI apply endpoint, and refuses unsupported apply actions.
