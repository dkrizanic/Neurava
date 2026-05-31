# Epic 3 Context: AI Memory Search And Source-Aware Assistant

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Epic 3 turns the notebook from a manual notes app into an AI memory surface. Users should be able to ask natural-language questions, recover history from vague fragments, inspect source-backed results, summarize prior context, and converse with an assistant that is structured around tool/action capabilities rather than a static chat-only implementation.

## Stories

- Story 3.1: AI Retrieval Index For Notes
- Story 3.2: Weak-Fragment AI Search
- Story 3.3: Source-Aware AI Answers
- Story 3.4: History Summary Generation
- Story 3.5: Conversational Assistant Panel
- Story 3.6: Tool-Action Assistant Foundation

## Requirements & Constraints

- AI memory search must support weak-fragment natural-language queries, such as remembered topics, phrases, or fuzzy descriptions, without requiring exact title/body matches.
- Search and assistant behavior must be scoped to the active Workspace Context. Results from unavailable contexts must never appear.
- AI-derived results must expose enough source metadata for trust and later source-aware answer generation. Relevant source types include notes first, with projects, calendar events, Gmail threads, dates, and reminders reserved for later stories.
- Search results should include user-useful context such as title, snippet, source type, and date.
- No-result states should help users refine the query instead of failing silently.
- OpenAI and other provider payloads must not leak into frontend responses, domain entities, or logs. Integration failures should be recoverable where possible and must not lose user data.
- Manual note search remains available independently of AI features, so semantic search should add capability without replacing the existing notes workflow.

## Technical Decisions

- PostgreSQL with pgvector is the MVP semantic retrieval store. It should combine relational filters for workspace/date/entity constraints with vector similarity for memory search.
- OpenAI access goes through Spring AI, but provider-specific payloads stay inside infrastructure boundaries. The current local embedding path may use deterministic embeddings where needed for stable local tests.
- Backend modules remain DDD-oriented and modular. Notes publish events; AI/search capabilities consume indexed records rather than reaching into note persistence internals.
- API resources use `/api/v1`, JSON resource responses, and RFC 7807-style problem details for unsupported or invalid requests.
- Dates/times exchanged by APIs should use ISO 8601 UTC strings.
- Future assistant operations should be modeled as typed tool/action inputs and outputs so search, summarize, and save-summary flows can be routed consistently.

## UX & Interaction Patterns

- AI search and assistant interfaces should clearly show the active Workspace Context.
- Result and assistant states should include loading, empty, and recoverable error feedback.
- Source material should be visually distinguishable from answer text once answers are introduced.
- Users should be able to open referenced sources from AI results as the source-reference surface matures.

## Cross-Story Dependencies

Story 3.1 prepares the retrieval index that Story 3.2 queries. Story 3.3 builds source-aware answers on top of the result/source metadata introduced by Story 3.2. Story 3.4 reuses source-aware retrieval and answer patterns for summaries. Story 3.5 provides the user-facing assistant panel for these capabilities. Story 3.6 formalizes the tool/action abstraction so search and summary operations can evolve without hard-coding assistant behavior.
