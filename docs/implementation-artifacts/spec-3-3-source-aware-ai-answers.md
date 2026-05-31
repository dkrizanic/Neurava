---
title: 'Source-Aware AI Answers'
type: 'feature'
created: '2026-05-31'
status: 'done'
baseline_commit: 'be4c0add1b523cfe4ef5c82e7cf00b885b4fcf7b'
epic: 3
story: 3
context:
  - '{project-root}/docs/implementation-artifacts/epic-3-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-2-weak-fragment-ai-search.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Semantic search can return relevant note matches, but the assistant still cannot answer a question with visible source references, so users cannot yet trust or inspect AI-assisted recall.

**Approach:** Add a source-aware answer endpoint that retrieves active-workspace note matches and produces an answer object with explicit source references, then replace the Assistant placeholder with a focused question form that displays answer text separately from sources and handles insufficient context.

## Boundaries & Constraints

**Always:** Scope answer retrieval to the active Workspace Context; include source references whenever retrieved content is used; distinguish answer text from source snippets in the API and UI; indicate when there is not enough source context; use `/api/v1` and RFC 7807-style validation errors; keep provider payloads, embeddings, and raw vectors server-side.

**Ask First:** Live OpenAI generation, multi-turn conversation history, saving answers as notes, summaries, Gmail/Calendar/project sources, and AI actions belong to later stories unless explicitly approved.

**Never:** Do not fabricate unsupported facts, return cross-workspace sources, expose embeddings, persist assistant conversations in this story, or make data-changing AI actions.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Source-backed answer | Signed-in user asks a question with relevant indexed notes | API returns answer text plus note source references; UI shows answer and sources as separate regions | N/A |
| Insufficient context | Query is valid but retrieval finds no useful note matches | API returns a response indicating insufficient context and no sources; UI says it lacks enough source context | N/A |
| Blank question | Question is missing or blank | Request is rejected before retrieval | API returns validation problem details; UI keeps the form usable |
| Workspace isolation | Another workspace has the only relevant note | Answer indicates insufficient context; no other-workspace source is exposed | No cross-context data is visible |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/MemorySearchService.java` -- existing workspace-scoped retrieval service for note matches.
- `apps/api/src/main/java/com/notebook/api/ai/application/MemorySearchMatch.java` -- existing source-style note match record for snippets, dates, and scores.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/MemorySearchController.java` -- pattern for authenticated AI web endpoints.
- `apps/api/src/test/java/com/notebook/api/ai/functional/NoteRetrievalIndexFunctionalTest.java` -- current AI retrieval functional test class.
- `apps/web/src/app/routes/SectionPage.tsx` -- Assistant route still renders a placeholder.
- `apps/web/src/features/assistant/types.ts` -- source-reference type already exists and should be extended for answer responses.
- `apps/web/src/features/search/components/SearchPage.tsx` -- useful UI pattern for question form, loading, empty, and error states.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/ai/application/**` -- add source-aware answer response/source records and service logic -- keeps answer assembly outside controllers.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/AssistantAnswerController.java` -- expose authenticated validated `/api/v1/ai/answers` endpoint -- gives frontend a stable assistant answer contract.
- [x] `apps/api/src/test/java/com/notebook/api/ai/**` -- cover source-backed answers, blank-question validation, insufficient context, and workspace isolation -- protects the I/O matrix.
- [x] `apps/web/src/features/assistant/**` and `apps/web/src/app/router.tsx` -- replace the Assistant placeholder with a question form, answer panel, source-reference list, and insufficient-context state -- completes the user-facing workflow.
- [x] `apps/web/src/features/assistant/**.test.tsx` -- cover answer with sources, insufficient context, and failed load messaging -- prevents UI regressions.

**Acceptance Criteria:**
- Given AI uses retrieved note content to answer a question, when the answer is displayed, then it includes visible Source References.
- Given an answer includes a Source Reference, when the user views it, then the source title, type, snippet, and date are visible enough to identify the note.
- Given answer text and source material are both present, when the Assistant route renders, then answer text is visually separated from source snippets.
- Given retrieval lacks enough source context, when the user asks a question, then the assistant clearly indicates it cannot answer from available sources.

## Design Notes

Keep this story source-aware and deterministic. Since live LLM generation is not approved here, answer text can be a conservative synthesis from the top retrieved note snippets, with explicit wording that it is based on available notebook sources. This is enough to validate the trust contract: answer text, source references, and insufficient-context behavior. Later stories can replace the synthesis engine with OpenAI while preserving the response shape.

## Result

Implemented. The API now exposes `/api/v1/ai/answers`, which builds a deterministic source-aware answer from active-workspace note retrieval matches and returns explicit note source references. The Assistant route is now a real user-facing flow with a question form, separated answer and source-reference regions, insufficient-context state, loading state, and recoverable error messaging.

## Verification

**Commands:**
- `.\mvnw.cmd test` from `apps/api` -- passed, 41 tests.
- `npm --prefix apps/web run test` -- passed, 24 tests.
- `npm --prefix apps/web run build` -- passed.
