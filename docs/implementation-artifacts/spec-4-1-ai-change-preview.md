---
title: 'AI Change Preview'
type: 'feature'
created: '2026-06-01'
status: 'done'
baseline_commit: 'fc2aec1ac0214c67cacffaf70f5f9884d9531333'
epic: 4
story: 1
context:
  - '{project-root}/docs/implementation-artifacts/epic-4-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-6-tool-action-assistant-foundation.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The assistant can retrieve, answer, summarize, and route typed actions, but it cannot yet propose a data-changing operation in a way the user can inspect before anything is persisted. Epic 4 needs a safe preview contract before apply/history/revert can be built.

**Approach:** Add a workspace-scoped AI action preview endpoint and Assistant UI flow for a first supported preview type: creating a note draft from user-provided text. The preview returns the entity type, action type, human-readable summary, and proposed note fields, but does not create a note, action history record, or any final user-visible data. Unsupported preview actions return sanitized problem details.

## Boundaries & Constraints

**Always:** Scope previews to the active Workspace Context; require explicit supported preview action names; show a preview before any apply affordance; keep existing answer/search/summary endpoints and action endpoint intact; return sanitized validation/problem details for invalid or unsupported preview requests; make it visually clear that the preview is not yet applied.

**Ask First:** Persisting previews, applying previews, creating AI Action History, revert, grammar-fix diffing, reminders/plans/projects, live OpenAI generation, streaming, and autonomous multi-step planning belong to later stories unless explicitly approved.

**Never:** Do not create or update notes during preview generation, create action history, execute arbitrary client-provided tool names, expose provider payloads/prompts/embeddings, or imply unsupported risky actions can be applied.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Create-note preview | Signed-in user requests `create_note` preview with messy text | API returns a note preview with title/body/tags proposal and no note is persisted; UI shows preview with Apply disabled or clearly marked as coming next | N/A |
| Blank input | Preview input text is missing or blank | Request is rejected before preview generation | API returns validation problem details; UI remains usable |
| Unsupported preview action | User requests unknown or risky preview action | No preview or data change occurs | API returns sanitized unsupported-action problem details |
| Workspace visibility | User previews a note in active workspace | Preview displays active Workspace Context | No cross-context data is read or written |
| Cancel preview | User dismisses preview in UI | Preview disappears from current session | No API apply call or persistence occurs |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/AssistantActionNames.java` -- existing explicit AI action-name pattern to extend or mirror.
- `apps/api/src/main/java/com/notebook/api/ai/application/AssistantActionService.java` -- typed dispatcher style to follow without mixing preview and read-only actions.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantActionController.java` -- authenticated active-workspace action endpoint and local problem-details mapping pattern.
- `apps/api/src/main/java/com/notebook/api/notes/application/NoteService.java` -- note creation service that must not be called by preview generation.
- `apps/api/src/test/java/com/notebook/api/ai/functional/NoteRetrievalIndexFunctionalTest.java` or a new AI preview functional test -- current AI endpoint functional coverage to extend.
- `apps/web/src/features/assistant/api/assistantApi.ts` and `types.ts` -- action API types/helpers to extend with preview client types.
- `apps/web/src/features/assistant/components/AssistantPage.tsx` -- current session-local assistant panel to add preview rendering.
- `apps/web/src/features/assistant/components/AssistantPage.test.tsx` -- assistant UI behavior coverage.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/ai/application/**` -- add preview action request/input/result records and a deterministic preview service for `create_note` -- creates a safe preview contract without persistence.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantActionPreviewController.java` -- expose authenticated validated `/api/v1/ai/action-previews` endpoint -- gives clients a dedicated preview surface.
- [x] `apps/api/src/test/java/com/notebook/api/ai/**` -- cover create-note preview, blank input, unsupported preview action, and no note persistence -- protects the safety boundary.
- [x] `apps/web/src/features/assistant/api/assistantApi.ts` and `types.ts` -- add typed preview helper and response types -- lets UI request previews without changing read-only action helpers.
- [x] `apps/web/src/features/assistant/components/AssistantPage.tsx` -- add a preview-capable mode or affordance that renders proposed note fields, active workspace, Cancel, and disabled/coming-next Apply state -- completes the user-facing preview.
- [x] `apps/web/src/features/assistant/components/AssistantPage.test.tsx` or preview API tests -- cover successful preview rendering, cancel behavior, unsupported/error state, and no read-only answer/summary regression -- prevents UI regressions.

**Acceptance Criteria:**
- Given the user asks AI to create a note preview, when the preview request succeeds, then the UI shows a proposed note title, body, tags, and summary before anything is applied.
- Given a preview is generated, when backend note data is inspected through existing note APIs, then no final note has been created by preview generation.
- Given the preview is shown, when the user cancels it, then the preview disappears and no apply/persistence request is made.
- Given an unsupported preview action is requested, when the API receives it, then it returns sanitized problem details and performs no data change.
- Given the Assistant page is used for existing answer and summary turns, when this story is complete, then those workflows remain unchanged.

## Design Notes

Keep Story 4.1 as a preview-only foundation. A deterministic note-draft generator is enough: derive a concise title from the first meaningful line, preserve/clean the provided text as body, and suggest lightweight tags from obvious words if useful. The important contract is safety: preview is visible, scoped, typed, and non-mutating.

## Verification

**Commands:**
- `.\mvnw.cmd test` from `apps/api` -- passed: 54 tests.
- `npm --prefix apps/web run test` -- passed: 8 files, 34 tests.
- `npm --prefix apps/web run build` -- passed: TypeScript and Vite build completed successfully.
- `git diff --check` -- passed.

## Result

Implemented. The API now exposes `/api/v1/ai/action-previews` with a supported `create_note` preview action that returns a deterministic note draft without persisting a note. Unsupported preview actions and invalid inputs return sanitized problem details from the AI preview controller boundary. The Assistant UI now includes a Preview mode that renders proposed note fields, shows Apply as a next-story disabled action, and lets the user cancel the current-session preview.
