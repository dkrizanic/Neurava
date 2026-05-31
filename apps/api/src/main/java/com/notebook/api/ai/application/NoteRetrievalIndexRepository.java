package com.notebook.api.ai.application;

import java.util.List;
import java.util.UUID;

public interface NoteRetrievalIndexRepository {

	void upsert(NoteRetrievalIndexRecord record);

	List<MemorySearchMatch> search(UUID workspaceContextId, EmbeddingVector queryEmbedding, String query, int limit);
}
