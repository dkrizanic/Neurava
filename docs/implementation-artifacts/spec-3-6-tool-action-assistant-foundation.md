---
title: 'Tool-Action Assistant Foundation'
type: 'feature'
created: '2026-05-31'
status: 'done'
baseline_commit: 'ca9faf8432fffe76cf5e3e48b900579d781f634f'
epic: 3
story: 6
context:
  - '{project-root}/docs/implementation-artifacts/epic-3-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-5-conversational-assistant-panel.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** AI search, answers, and summaries now exist, but assistant behavior is still wired as separate feature calls. Future assistant capabilities need a typed tool/action foundation so the product can route supported operations consistently without pretending arbitrary chat actions exist.

**Approach:** Add a backend assistant action dispatcher with explicit action names, typed request/response contracts, active-workspace scoping, and problem-details handling for unsupported actions. Expose a single `/api/v1/ai/actions` endpoint that routes to existing search, answer, and summary services while preserving the existing dedicated endpoints. Add frontend API/types and a small internal adapter so the Assistant panel can call the action endpoint for answer and summary turns without changing its user-facing behavior.

## Boundaries & Constraints

**Always:** Scope every action to the active Workspace Context; keep existing `/api/v1/ai/search`, `/api/v1/ai/answers`, and `/api/v1/ai/summaries` contracts working; return typed action results; reject unsupported action names with sanitized RFC 7807 problem details; keep provider payloads, vectors, and internal prompts out of API responses.

**Ask First:** Data-changing assistant actions, action persistence/audit history, tool marketplace/MCP integration, streaming tool calls, live OpenAI tool-calling, and multi-step autonomous planning belong to later stories unless explicitly approved.

**Never:** Do not execute arbitrary client-provided tool names, perform note writes/deletes, return cross-workspace sources, expose embeddings/provider internals, or remove the dedicated endpoints that existing UI surfaces already use.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Answer action | Signed-in user posts `{ "action": "answer_question", "input": { "question": "..." } }` | API returns `{ "action": "answer_question", "result": { ...SourceAwareAnswer } }` scoped to active workspace | N/A |
| Summary action | Signed-in user posts `{ "action": "summarize_history", "input": { "topic": "..." } }` | API returns `{ "action": "summarize_history", "result": { ...HistorySummary } }` scoped to active workspace | N/A |
| Search action | Signed-in user posts `{ "action": "search_memory", "input": { "query": "..." } }` | API returns `{ "action": "search_memory", "result": [ ...MemorySearchMatch ] }` scoped to active workspace | N/A |
| Unsupported action | User posts an unknown action name | Request is rejected before any service dispatch | API returns sanitized problem details with a 400-class status |
| Invalid input shape | Action exists but required input is missing/blank/too long | Request is rejected before service dispatch | API returns validation problem details and no provider/internal details |
| Workspace isolation | Relevant notes exist in another workspace | Action uses only active workspace data | No cross-context data is returned |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/MemorySearchService.java` -- existing search capability to route through an action.
- `apps/api/src/main/java/com/notebook/api/ai/application/SourceAwareAnswerService.java` -- existing answer capability to route through an action.
- `apps/api/src/main/java/com/notebook/api/ai/application/HistorySummaryService.java` -- existing summary capability to route through an action.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/*Controller.java` -- authenticated active-workspace controller patterns to reuse.
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/errors/GlobalApiExceptionHandler.java` -- problem-details handling to extend for unsupported actions if needed.
- `apps/api/src/test/java/com/notebook/api/ai/functional/NoteRetrievalIndexFunctionalTest.java` -- current AI functional coverage to extend.
- `apps/web/src/features/assistant/api/assistantApi.ts` and `types.ts` -- frontend assistant contracts to adapt to action results.
- `apps/web/src/features/assistant/components/AssistantPage.test.tsx` -- behavior should remain stable while transport changes.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/ai/application/**` -- add assistant action name/input/result records plus dispatcher service -- centralizes supported assistant capabilities.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantActionController.java` -- expose authenticated validated `/api/v1/ai/actions` endpoint -- gives assistant clients one typed action surface.
- [x] `apps/api/src/main/java/com/notebook/api/shared/infrastructure/errors/GlobalApiExceptionHandler.java` or AI-specific exception mapping -- return sanitized problem details for unsupported actions -- prevents internal exception leakage.
- [x] `apps/api/src/test/java/com/notebook/api/ai/**` -- cover answer, summary, search, unsupported action, invalid input, and workspace isolation through the action endpoint -- protects routing and boundaries.
- [x] `apps/web/src/features/assistant/api/assistantApi.ts` and `types.ts` -- add typed action client helpers while preserving current answer/summary helper signatures for the page -- avoids UI churn.
- [x] `apps/web/src/features/assistant/api/assistantApi.test.ts` -- assert answer and summary helpers call the action-backed API adapter -- prevents transport regressions.

**Acceptance Criteria:**
- Given the assistant client invokes `answer_question`, `summarize_history`, or `search_memory`, when the action is supported, then the API dispatches to the existing workspace-scoped service and returns a typed result envelope.
- Given an unknown action is requested, when the API receives it, then it returns sanitized problem details and does not call any AI capability service.
- Given action input is blank, missing, or too long, when validation runs, then the API returns validation problem details and performs no retrieval.
- Given notes in another Workspace Context match an action input, when the action is executed, then those sources are not used or exposed.
- Given the Assistant page is used for answer or summary turns, when requests complete or fail, then current session behavior, source references, retry, and error states remain unchanged.

## Design Notes

Prefer a small explicit registry/dispatcher over reflection or dynamic tool execution. Action names should be stable string constants, but implementation can remain simple records/classes until there is a real need for a larger tool framework. This story is about the product contract and routing boundary, not autonomous AI planning.

## Verification

**Commands:**
- `.\mvnw.cmd test` from `apps/api` -- passed: 51 tests.
- `npm --prefix apps/web run test` -- passed: 8 files, 30 tests.
- `npm --prefix apps/web run build` -- passed: TypeScript and Vite build completed successfully.
- `git diff --check` -- passed.

## Result

Implemented. The API now exposes `/api/v1/ai/actions` with explicit `answer_question`, `summarize_history`, and `search_memory` dispatch through existing active-workspace services. Unsupported action names and invalid action inputs return sanitized problem details from the AI controller boundary. The existing dedicated AI endpoints remain intact, and the Assistant frontend now uses action-backed helper adapters without changing the session panel behavior.
