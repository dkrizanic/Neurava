package com.notebook.api.memory.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.notebook.api.memory.application.NoteRetrievalIndexRecord;
import com.notebook.api.memory.application.NoteRetrievalIndexRepository;

@Repository
class JdbcNoteRetrievalIndexRepository implements NoteRetrievalIndexRepository {

	private final JdbcTemplate jdbc;

	JdbcNoteRetrievalIndexRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void upsert(NoteRetrievalIndexRecord record) {
		this.jdbc.update("""
				INSERT INTO note_retrieval_index (
				  note_id, workspace_context_id, owner_account_id, title, searchable_text, embedding,
				  indexed_at, note_updated_at, status, failure_message
				)
				VALUES (?, ?, ?, ?, ?, ?::vector, ?, ?, ?, ?)
				ON CONFLICT (note_id) DO UPDATE SET
				  workspace_context_id = EXCLUDED.workspace_context_id,
				  owner_account_id = EXCLUDED.owner_account_id,
				  title = EXCLUDED.title,
				  searchable_text = EXCLUDED.searchable_text,
				  embedding = EXCLUDED.embedding,
				  indexed_at = EXCLUDED.indexed_at,
				  note_updated_at = EXCLUDED.note_updated_at,
				  status = EXCLUDED.status,
				  failure_message = EXCLUDED.failure_message
				""",
				record.noteId(),
				record.workspaceContextId(),
				record.ownerAccountId(),
				record.title(),
				record.searchableText(),
				record.embedding().toPgVectorLiteral(),
				OffsetDateTime.ofInstant(record.indexedAt(), ZoneOffset.UTC),
				OffsetDateTime.ofInstant(record.noteUpdatedAt(), ZoneOffset.UTC),
				record.status().name(),
				record.failureMessage());
	}
}
