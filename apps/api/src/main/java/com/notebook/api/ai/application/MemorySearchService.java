package com.notebook.api.ai.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MemorySearchService {

	private static final int DEFAULT_LIMIT = 8;

	private final EmbeddingGenerator embeddings;
	private final NoteRetrievalIndexRepository index;

	public MemorySearchService(EmbeddingGenerator embeddings, NoteRetrievalIndexRepository index) {
		this.embeddings = embeddings;
		this.index = index;
	}

	public List<MemorySearchMatch> searchNotes(UUID workspaceContextId, String query) {
		String normalizedQuery = query.trim();
		EmbeddingVector queryEmbedding = this.embeddings.embed(normalizedQuery);
		return this.index.search(workspaceContextId, queryEmbedding, normalizedQuery, DEFAULT_LIMIT);
	}
}
