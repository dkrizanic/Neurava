---
title: 'AI Retrieval Index For Notes'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 3
story: 1
context:
  - '{project-root}/docs/implementation-artifacts/spec-2-6-selectable-professional-editor-modes.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Notes are editable and workspace-scoped, but they are not prepared for semantic retrieval, so later AI search cannot find relevant history by meaning.

**Approach:** Publish note content change events from the notes module and let a separate AI module maintain PostgreSQL/pgvector retrieval records containing safe searchable text, workspace metadata, and embeddings.

## Boundaries & Constraints

**Always:** Retrieval records are scoped to workspace context id; notes must not call AI services directly; indexing failures must be logged and must not roll back note saves; stored text must be title/body metadata only.

**Ask First:** User-facing semantic search, answer generation, OpenAI provider tuning, chunking strategies, and reindex jobs belong to later stories.

**Never:** Do not expose embeddings to the frontend, leak OAuth/provider payloads, or import note persistence entities into the AI module.

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V8__note_retrieval_index.sql` -- pgvector-backed retrieval table.
- `apps/api/src/main/java/com/notebook/api/notes/application/NoteContentChangedEvent.java` -- public note event emitted on content saves.
- `apps/api/src/main/java/com/notebook/api/ai/**` -- AI module listener, embedding generator, and JDBC index repository.
- `apps/api/src/test/java/com/notebook/api/ai/**` -- indexing service and listener coverage.
- `apps/api/src/test/java/com/notebook/api/notes/**` -- event publishing coverage.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V8__note_retrieval_index.sql` -- add pgvector index storage -- persists embeddings with workspace scope.
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- publish note content events -- decouples notes from AI indexing.
- [x] `apps/api/src/main/java/com/notebook/api/ai/**` -- index note content from events -- prepares searchable text and embedding vectors.
- [x] `apps/api/src/test/java/com/notebook/api/**` -- test event publishing and listener failure safety -- protects strict module behavior.

**Acceptance Criteria:**
- Given a note is created, when it is saved, then a retrieval index record is prepared for that note.
- Given a note is updated, when content changes are saved, then the retrieval record is refreshed with new text and updated timestamp.
- Given indexing fails, when the note save completes, then the failure is logged safely without rolling back the note.
- Given retrieval records are stored, when inspected, then each record includes workspace context id and a pgvector embedding.

## Result

Implemented. Notes now publish content-change events after create/update, and the AI module handles those events after commit to maintain a workspace-scoped pgvector retrieval index. The implementation keeps notes independent from AI concerns, stores only title/body searchable text plus metadata, and logs indexing failures without rolling back note saves.

## Verification

**Commands:**
- `npm run test:api:unit` -- passed, 17 tests.
- `npm run test:api:functional` -- passed, 15 tests with PostgreSQL available.
- `npm --prefix apps/web run test` -- passed, 16 tests.
- `npm --prefix apps/web run build` -- passed.
