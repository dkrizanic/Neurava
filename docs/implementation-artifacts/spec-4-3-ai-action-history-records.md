---
title: 'AI Action History Records'
type: 'feature'
created: '2026-06-01'
status: 'done'
epic: 4
story: 3
context:
  - '{project-root}/docs/implementation-artifacts/epic-4-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-4-2-apply-ai-actions.md'
---

## Intent

**Problem:** Approved AI actions can now create notes, but those changes are not durably recorded for later inspection.

**Approach:** Add a workspace-scoped AI Action History table, record `create_note` applications after persistence, expose recent history through an authenticated API, and show recent AI changes in the Assistant screen.

## Boundaries & Constraints

**Always:** Record applied AI data changes with action, changed entity, previous state, current state, summary, timestamp, user, and Workspace Context. Keep manual note edits outside AI history. Preserve module boundaries by depending on notes application summaries only.

**Ask First:** Revert behavior, action-history deletion policy, detailed diff UI, and non-note action history belong to later stories.

**Never:** Record preview-only actions as applied changes, leak provider payloads/prompts/embeddings, or store cross-workspace history.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Applied note creation | User applies `create_note` | History record is created with note current state and no previous state | N/A |
| List history | Signed-in user opens Assistant | Recent AI changes are loaded for active workspace | Recoverable warning if load fails |
| Workspace scope | Different workspace/user has history | Only active workspace records are returned | No cross-workspace data |
| Manual note edit | User edits notes manually | No AI history record is required | N/A |

## Code Map

- `apps/api/src/main/resources/db/migration/V11__ai_action_history.sql` -- creates durable AI history storage.
- `apps/api/src/main/java/com/notebook/api/ai/domain/AiActionHistoryRecord.java` -- persistence model for applied AI changes.
- `apps/api/src/main/java/com/notebook/api/ai/application/AiActionHistoryService.java` -- records and lists history summaries.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AiActionHistoryController.java` -- exposes `/api/v1/ai/action-history`.
- `apps/web/src/features/assistant/**` -- loads and renders recent AI changes.

## Tasks & Acceptance

**Execution:**
- [x] Add AI Action History migration, entity, repository, summary DTO, and service.
- [x] Record `create_note` application history after the note is persisted.
- [x] Add authenticated list endpoint scoped to active workspace.
- [x] Add functional coverage for applied action history fields.
- [x] Add frontend API helper and Assistant recent-history section.

**Acceptance Criteria:**
- Given an AI action is applied, when the action completes, then an AI Action History record is created.
- Given the history record is stored, then it includes changed entity, previous state, current state, summary, timestamp, user, and Workspace Context.
- Given the Assistant screen loads, then recent AI changes are visible for the active workspace.

## Verification

**Commands:**
- `.\mvnw.cmd -q -Dtest=NoteRetrievalIndexFunctionalTest test` from `apps/api` -- passed.
- `.\mvnw.cmd -q test` from `apps/api` -- passed: 58 tests.
- `npm --prefix apps/web test -- --run` -- passed: 8 files, 32 tests.
- `npm --prefix apps/web run build` -- passed.

## Result

Implemented Story 4.3. Applied AI note creations now produce durable, workspace-scoped AI Action History records, the backend exposes recent history, and the Assistant page shows recent AI changes.
