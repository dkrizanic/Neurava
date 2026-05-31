# Epic 4 Context: Safe AI Actions And Revertible Changes

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 4 turns the assistant from source-backed recall into a controlled action surface. Users should be able to ask AI to propose supported changes, inspect a preview before anything is persisted, apply approved changes, review durable AI Action History, and revert applied AI changes where possible.

## Stories

- Story 4.1: AI Change Preview
- Story 4.2: Apply AI Actions
- Story 4.3: AI Action History Records
- Story 4.4: Revert AI Actions
- Story 4.5: One-Button Grammar Fix
- Story 4.6: Generate Note From Messy Input

## Requirements & Constraints

- AI Assistant can propose supported changes to app data, but users must see a preview before applying.
- Preview generation must not persist final user-visible changes.
- Applying AI changes is a separate explicit user action.
- Supported applied changes later include creating notes, reminders, plans, project summaries, tags/categories, saved summaries, and entity links.
- Risky or unsupported actions must be refused or routed to explicit manual user action.
- Every AI action that changes data must eventually create durable AI Action History with changed entities, previous state, current state, user-visible summary, timestamp, user, and Workspace Context.
- AI Action History must support revert where possible and explain when revert cannot proceed.
- Workspace Context scoping applies to previews, applied changes, history, and revert.
- AI/provider payloads, prompts, embeddings, OAuth tokens, and sensitive source content must not leak in logs or API responses.

## Technical Decisions

- Story 3.6 introduced `/api/v1/ai/actions` and a typed assistant action dispatcher for non-mutating actions.
- Epic 4 should build on that explicit action model rather than executing arbitrary client-provided tool names.
- Preview/apply/revert should be modeled as command-style endpoints with typed inputs and outputs.
- MVP preview generation can be deterministic and source-aware; live OpenAI generation can improve quality later without changing the preview/apply contract.
- No data-changing operation should occur during preview creation.
- REST APIs use `/api/v1`, JSON responses, validation, and sanitized RFC 7807 problem details.

## UX & Interaction Patterns

- Assistant UI must show previews before data-changing actions.
- Preview content should clearly explain what entity type would change, which fields would be created or modified, and what the user can do next.
- Users must have clear Apply and Cancel affordances.
- Cancel should leave no final user-visible changes.
- Loading, empty, validation, unsupported action, and recoverable error states should be explicit.
- The active Workspace Context should remain visible whenever an AI action is previewed or applied.

## Cross-Story Dependencies

Story 4.1 introduces the preview contract and UI pattern. Story 4.2 applies approved previews. Story 4.3 records applied AI action history. Story 4.4 reverts applied actions. Stories 4.5 and 4.6 add concrete editor/note actions on top of the preview/apply/history/revert foundation.
