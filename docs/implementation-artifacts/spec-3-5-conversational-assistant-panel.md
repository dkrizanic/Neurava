---
title: 'Conversational Assistant Panel'
type: 'feature'
created: '2026-05-31'
status: 'done'
baseline_commit: 'ac170454ab42ac3fc3abca69a550dcf5d7b6706c'
epic: 3
story: 5
context:
  - '{project-root}/docs/implementation-artifacts/epic-3-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-3-4-history-summary-generation.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The Assistant route supports one-off answer and summary requests, but it does not feel conversational: users cannot see a current-session message history, refine from prior turns, or clearly track the assistant's active workspace context.

**Approach:** Convert the Assistant route into a session-local conversational panel that appends user and assistant messages for answer and summary requests, preserves source references per assistant response, and shows active workspace, loading, error, and recoverable retry states.

## Boundaries & Constraints

**Always:** Keep conversation history scoped to the current browser session only; show the active Workspace Context clearly; preserve source references with each assistant response; allow answer and summary interactions from the same panel; keep existing `/api/v1/ai/answers` and `/api/v1/ai/summaries` contracts intact.

**Ask First:** Persisting conversations, live OpenAI chat, server-side conversation state, streaming responses, clarifying-question generation by an LLM, and AI data-changing actions belong to later stories unless explicitly approved.

**Never:** Do not store conversation history in the database or local storage, expose embeddings/provider payloads, remove Source References, or make the UI imply data-changing AI actions are supported.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Answer turn | User sends a question in Answer mode | User message and assistant answer message are appended; sources stay attached to the assistant message | N/A |
| Summary turn | User sends a topic in Summary mode | User message and structured assistant summary message are appended; sources stay attached | N/A |
| Broad/ambiguous prompt | User enters a very short or broad prompt | UI keeps the prompt and shows a clarifying assistant message instead of calling retrieval | No API call is made |
| Failed request | API request fails | User message remains visible and assistant shows a recoverable error with retry affordance | No typed content/history is lost |

</frozen-after-approval>

## Code Map

- `apps/web/src/features/assistant/components/AssistantPage.tsx` -- current one-shot Assistant answer/summary page.
- `apps/web/src/features/assistant/components/AssistantPage.test.tsx` -- current assistant UI coverage.
- `apps/web/src/features/assistant/api/assistantApi.ts` -- answer and summary API calls to preserve.
- `apps/web/src/features/assistant/types.ts` -- answer, summary, source-reference response types.
- `apps/web/src/app/styles/globals.css` -- assistant layout and message styles.

## Tasks & Acceptance

**Execution:**
- [x] `apps/web/src/features/assistant/types.ts` -- add session message view types -- makes answer/summary/error turns explicit.
- [x] `apps/web/src/features/assistant/components/AssistantPage.tsx` -- convert one-shot rendering to append-only current-session message history with answer/summary modes, active workspace context, clarifying message, retry, loading, and error states -- completes the conversational panel.
- [x] `apps/web/src/app/styles/globals.css` -- add compact chat/message/source styling that remains responsive -- keeps the panel polished on desktop and narrow viewports.
- [x] `apps/web/src/features/assistant/components/AssistantPage.test.tsx` -- cover answer turn history, summary turn history, ambiguous prompt clarification, and failed request retry behavior -- prevents regressions.

**Acceptance Criteria:**
- Given the user opens Assistant, when they send messages, then the current session shows user and assistant message history in order.
- Given the assistant responds with sources, when the response appears, then Source References remain visible with that specific response.
- Given the user asks a broad or ambiguous request, when the prompt is too thin to retrieve useful context, then the assistant asks for more detail instead of silently failing.
- Given an answer or summary request fails, when the error appears, then the user can retry without losing prior messages.
- Given the Assistant route renders, when viewed, then the active Workspace Context is visible.

## Design Notes

Keep this story frontend-focused. The backend already provides answer and summary primitives; this story turns them into a conversational experience without introducing server-side chat state. A simple local array of messages is enough for MVP and avoids pretending the assistant has durable memory before the product has that contract.

## Verification

**Commands:**
- `npm --prefix apps/web run test` -- passed: 7 files, 28 tests.
- `npm --prefix apps/web run build` -- passed: TypeScript and Vite build completed successfully.
- `.\mvnw.cmd test` from `apps/api` -- passed: 45 tests.
- `git diff --check` -- passed.

## Result

Implemented. The Assistant route now keeps current-session message history, supports answer and summary turns in the same panel, preserves source references on each response, shows the active workspace context, asks for more detail for single-token prompts without calling retrieval, and provides a retry action for failed answer or summary requests.
