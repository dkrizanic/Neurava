package com.notebook.api.memory.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.memory.domain.RetrievalIndexStatus;

public record NoteRetrievalIndexRecord(
		UUID noteId,
		UUID workspaceContextId,
		UUID ownerAccountId,
		String title,
		String searchableText,
		EmbeddingVector embedding,
		Instant indexedAt,
		Instant noteUpdatedAt,
		RetrievalIndexStatus status,
		String failureMessage
) {
}
