---
title: 'History Summary Generation'
type: 'feature'
created: '2026-05-31'
status: 'done'
baseline_commit: '719f806b25bab4d9552157db3f2bf5ce704ab70e'
epic: 3
story: 4
context:
  - '{project-root}/docs/implementation-artifacts/epic-3-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-3-source-aware-ai-answers.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The assistant can answer a single question with sources, but users cannot yet ask for a structured history summary that gathers relevant note context into events, decisions, unresolved items, and next actions.

**Approach:** Add a deterministic source-aware summary endpoint over active-workspace retrieved note matches, then extend the Assistant UI with a summary mode that shows structured sections, source references, and insufficient-context behavior.

## Boundaries & Constraints

**Always:** Scope summary retrieval to the active Workspace Context; include Source References for every generated summary; clearly separate generated summary sections from source snippets; indicate when there is not enough source context; use `/api/v1` validation/problem-details conventions; keep raw embeddings and provider payloads server-side.

**Ask First:** Live OpenAI generation, saving the summary as a note, time-range filters, project/plan/calendar/Gmail sources, multi-turn conversation history, and AI action history belong to later stories unless explicitly approved.

**Never:** Do not fabricate unsupported events or decisions, return cross-workspace sources, persist generated summaries in this story, expose vectors, or perform data-changing AI actions.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Source-backed summary | Signed-in user asks to summarize a topic with relevant indexed notes | API returns structured summary sections and note source references; UI displays the sections and sources separately | N/A |
| Insufficient context | Topic is valid but retrieval finds no useful matches | API returns insufficient-context summary response; UI says there is not enough source context | N/A |
| Blank topic | Topic/question is missing or blank | Request is rejected before retrieval | API returns validation problem details; UI keeps the form usable |
| Workspace isolation | Another workspace has the relevant notes | Summary indicates insufficient context and exposes no other-workspace source | No cross-context data is visible |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/MemorySearchService.java` -- workspace-scoped retrieval foundation.
- `apps/api/src/main/java/com/notebook/api/ai/application/SourceAwareAnswerService.java` -- deterministic source-aware answer pattern to mirror for summaries.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantAnswerController.java` -- authenticated AI endpoint pattern.
- `apps/api/src/test/java/com/notebook/api/ai/functional/NoteRetrievalIndexFunctionalTest.java` -- current AI retrieval/answer functional coverage.
- `apps/web/src/features/assistant/components/AssistantPage.tsx` -- current Assistant question form and source-reference layout.
- `apps/web/src/features/assistant/api/assistantApi.ts` and `types.ts` -- frontend assistant API and response contracts.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/ai/application/**` -- add source-aware history summary response records and service logic -- keeps summary assembly outside controllers.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/HistorySummaryController.java` -- expose authenticated validated `/api/v1/ai/summaries` endpoint -- gives frontend a stable summary contract.
- [x] `apps/api/src/test/java/com/notebook/api/ai/**` -- cover structured summary, blank-topic validation, insufficient context, and workspace isolation -- protects the I/O matrix.
- [x] `apps/web/src/features/assistant/**` -- add summary mode/API/types and render structured sections plus source references -- completes the user-facing workflow in the existing Assistant surface.
- [x] `apps/web/src/features/assistant/**.test.tsx` -- cover summary with sources, insufficient context, and failed load messaging -- prevents UI regressions.

**Acceptance Criteria:**
- Given relevant source content exists in the active Workspace Context, when the user asks for a history summary, then the assistant shows key events, decisions, unresolved items, and next actions where source text supports them.
- Given a summary is displayed, when the user reviews it, then Source References are visible and separate from generated summary sections.
- Given retrieval lacks enough source context, when the user asks for a summary, then the assistant clearly indicates it cannot summarize from available sources.
- Given notes in another Workspace Context are relevant, when the user asks for a summary, then those notes are not used or exposed.

## Design Notes

This story should reuse the deterministic style from Story 3.3. Without live LLM approval, classify summary bullets conservatively from retrieved snippets using simple textual signals and neutral fallback phrasing. The goal is to lock the product contract and UI shape for source-backed summaries; future OpenAI generation can improve prose without changing the response shape.

## Result

Implemented. The API now exposes `/api/v1/ai/summaries`, which returns active-workspace source-backed summary sections and note Source References. The Assistant route now supports Answer and Summary modes, renders structured key events, decisions, unresolved items, and next actions separately from source snippets, and preserves insufficient-context and recoverable error states.

## Verification

**Commands:**
- `.\mvnw.cmd test` from `apps/api` -- passed, 45 tests.
- `npm --prefix apps/web run test` -- passed, 27 tests.
- `npm --prefix apps/web run build` -- passed.
