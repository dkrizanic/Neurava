---
title: 'Weak-Fragment AI Search'
type: 'feature'
created: '2026-05-31'
status: 'done'
baseline_commit: 'd7f313843d451e0ccdac724adc169cdf0184b77e'
epic: 3
story: 2
context:
  - '{project-root}/docs/implementation-artifacts/epic-3-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-1-ai-retrieval-index-for-notes.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Notes are indexed for semantic retrieval, but users still cannot search by vague remembered fragments or inspect AI-ready matches from the app.

**Approach:** Add a workspace-scoped semantic note search endpoint over the retrieval index, then replace the placeholder Search route with a focused weak-fragment search UI that renders match metadata, snippets, loading/error, and no-result states.

## Boundaries & Constraints

**Always:** Limit results to the signed-in user's active Workspace Context; return source metadata suitable for later source-aware answers; keep embeddings and provider internals server-side; use `/api/v1` and problem details for invalid requests; keep manual note search intact.

**Ask First:** Introducing live OpenAI embedding calls, broad multi-source search beyond notes, pagination, ranking-tuning UX, or assistant answer generation belongs to later stories unless explicitly approved.

**Never:** Do not expose vector values to the frontend, query notes from another workspace, import note persistence entities into the AI module, or present semantic search as a generated AI answer.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Semantic note match | Signed-in user searches "api problem" with indexed notes in active workspace | API returns ranked matches with note id, title, snippet, source type `note`, date, and score; UI displays the matches | N/A |
| Blank query | Search query is missing or blank | Request is rejected before retrieval | API returns validation problem details; UI keeps the form usable |
| No matches | Query is valid but no indexed note is relevant enough | API returns an empty list; UI shows a refinement-oriented empty state | N/A |
| Workspace isolation | Another workspace has a better semantic match | Only active-workspace matches are returned | No cross-context data is visible |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/ai/application/EmbeddingGenerator.java` -- existing abstraction for embedding query text.
- `apps/api/src/main/java/com/notebook/api/ai/application/NoteRetrievalIndexRepository.java` -- add search contract over indexed records.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/persistence/JdbcNoteRetrievalIndexRepository.java` -- implement pgvector similarity query with workspace filtering.
- `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/MemorySearchController.java` -- new authenticated `/api/v1/ai/search` entry point.
- `apps/api/src/test/java/com/notebook/api/ai/functional/NoteRetrievalIndexFunctionalTest.java` -- extend functional coverage for indexed semantic search.
- `apps/web/src/app/routes/SectionPage.tsx` -- route currently renders the Search placeholder.
- `apps/web/src/features/assistant/types.ts` -- source reference type can be reused/extended for note matches.
- `apps/web/src/features/search/**` -- new feature API/types/components for weak-fragment search.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/java/com/notebook/api/ai/application/**` -- add search request/result records and service logic -- centralizes retrieval behavior outside web/persistence.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/persistence/JdbcNoteRetrievalIndexRepository.java` -- query indexed notes by cosine distance with active workspace filtering and a small result limit -- satisfies semantic retrieval without exposing vectors.
- [x] `apps/api/src/main/java/com/notebook/api/ai/infrastructure/web/MemorySearchController.java` -- expose authenticated search endpoint with validation -- gives the frontend a stable contract.
- [x] `apps/api/src/test/java/com/notebook/api/ai/**` -- cover ranked results, blank-query validation, no-result behavior, and workspace isolation -- protects the I/O matrix.
- [x] `apps/web/src/features/search/**` and `apps/web/src/app/routes/SectionPage.tsx` -- replace the Search placeholder with query input, result list, snippets, source/date labels, and loading/error/empty states -- completes the user-facing workflow.
- [x] `apps/web/src/features/search/**.test.tsx` -- cover successful results, empty state, and failed load messaging -- prevents UI regressions.

**Acceptance Criteria:**
- Given a signed-in user has indexed notes in the active Workspace Context, when they search with a vague fragment, then relevant note matches are displayed with title, snippet, source type, and date.
- Given indexed notes exist in another Workspace Context, when the user searches from their active context, then those notes are not returned.
- Given semantic search has no useful matches, when results load, then the UI suggests refining the query without clearing the user's input.
- Given AI search fails, when the user views the Search route, then the UI shows a recoverable error state and the notes workflow remains unaffected.

## Design Notes

Keep Story 3.2 as retrieval, not answer generation. A match is a source candidate, so the response should be shaped like a source/result object rather than chat text. The endpoint can start note-only while using generic names such as `sourceType` and `sourceId` so Story 3.3 can attach answers and Story 3.4 can attach summaries without reshaping the contract.

## Result

Implemented. Signed-in users can search indexed notes from the Search route using weak remembered fragments. The backend exposes a workspace-scoped `/api/v1/ai/search` endpoint over the retrieval index, returns note source matches with snippets, dates, source type, and score, and rejects blank queries with problem details. The frontend replaces the placeholder Search route with a real query form, result list, loading state, empty refinement state, and recoverable error messaging.

## Verification

**Commands:**
- `.\mvnw.cmd test` from `apps/api` -- passed, 37 tests.
- `npm --prefix apps/web run test` -- passed on rerun, 20 tests. First run hit a Notes autosave timing flake.
- `npm --prefix apps/web run build` -- passed.
