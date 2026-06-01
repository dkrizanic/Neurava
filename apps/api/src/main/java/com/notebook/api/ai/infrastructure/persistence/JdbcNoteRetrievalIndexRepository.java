package com.notebook.api.ai.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.notebook.api.ai.application.EmbeddingVector;
import com.notebook.api.ai.application.MemorySearchMatch;
import com.notebook.api.ai.application.NoteRetrievalIndexRecord;
import com.notebook.api.ai.application.NoteRetrievalIndexRepository;

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

	@Override
	public void delete(UUID noteId, UUID workspaceContextId) {
		this.jdbc.update("""
				DELETE FROM note_retrieval_index
				WHERE note_id = ?
				  AND workspace_context_id = ?
				""", noteId, workspaceContextId);
	}

	@Override
	public List<MemorySearchMatch> search(UUID workspaceContextId, EmbeddingVector queryEmbedding, String query, int limit) {
		List<String> tokens = tokens(query);
		List<IndexedNoteMatch> candidates = this.jdbc.query("""
				SELECT note_id, title, searchable_text, note_updated_at, embedding <=> ?::vector AS distance
				FROM note_retrieval_index
				WHERE workspace_context_id = ?
				  AND status = 'INDEXED'
				ORDER BY embedding <=> ?::vector, note_updated_at DESC
				LIMIT ?
				""",
				(resultSet, rowNumber) -> mapMatch(resultSet, tokens),
				queryEmbedding.toPgVectorLiteral(),
				workspaceContextId,
				queryEmbedding.toPgVectorLiteral(),
				Math.max(limit * 4, limit));

		return candidates.stream()
				.filter(match -> match.lexicalScore() > 0 || match.vectorSimilarity() >= 0.85d)
				.sorted(Comparator.comparingDouble(IndexedNoteMatch::score).reversed()
						.thenComparing(IndexedNoteMatch::sourceUpdatedAt, Comparator.reverseOrder()))
				.limit(limit)
				.map(IndexedNoteMatch::toSearchMatch)
				.toList();
	}

	private static IndexedNoteMatch mapMatch(ResultSet resultSet, List<String> tokens) throws SQLException {
		String searchableText = resultSet.getString("searchable_text");
		double vectorSimilarity = 1.0d - resultSet.getDouble("distance");
		int lexicalScore = lexicalScore(searchableText, tokens);
		double score = Math.min(1.0d, (lexicalScore * 0.18d) + Math.max(0.0d, vectorSimilarity * 0.46d));
		return new IndexedNoteMatch(
				resultSet.getObject("note_id", UUID.class),
				resultSet.getString("title"),
				snippet(searchableText, tokens),
				resultSet.getObject("note_updated_at", OffsetDateTime.class).toInstant(),
				vectorSimilarity,
				lexicalScore,
				score);
	}

	private static List<String> tokens(String query) {
		return Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
				.filter(token -> token.length() >= 3)
				.distinct()
				.toList();
	}

	private static int lexicalScore(String searchableText, List<String> tokens) {
		String normalizedText = searchableText.toLowerCase(Locale.ROOT);
		int score = 0;
		for (String token : tokens) {
			if (normalizedText.contains(token)) {
				score++;
			}
		}
		return score;
	}

	private static String snippet(String searchableText, List<String> tokens) {
		String compactText = searchableText.replaceFirst("^Title: .*\\R\\R", "").replaceAll("\\s+", " ").trim();
		if (compactText.isBlank()) {
			compactText = searchableText.replaceAll("\\s+", " ").trim();
		}

		String normalizedText = compactText.toLowerCase(Locale.ROOT);
		int start = 0;
		for (String token : tokens) {
			int index = normalizedText.indexOf(token);
			if (index >= 0) {
				start = Math.max(0, index - 56);
				break;
			}
		}

		int end = Math.min(compactText.length(), start + 180);
		String prefix = start > 0 ? "... " : "";
		String suffix = end < compactText.length() ? " ..." : "";
		return prefix + compactText.substring(start, end).trim() + suffix;
	}

	private record IndexedNoteMatch(
			UUID sourceId,
			String title,
			String snippet,
			java.time.Instant sourceUpdatedAt,
			double vectorSimilarity,
			int lexicalScore,
			double score
	) {
		MemorySearchMatch toSearchMatch() {
			return new MemorySearchMatch(this.sourceId, "note", this.title, this.snippet, this.sourceUpdatedAt, this.score);
		}
	}
}
