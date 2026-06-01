package com.notebook.api.ai.application;

import java.util.List;
import java.util.UUID;

public interface NoteRetrievalIndexRepository {

	void upsert(NoteRetrievalIndexRecord record);

	void delete(UUID noteId, UUID workspaceContextId);

	List<MemorySearchMatch> search(UUID workspaceContextId, EmbeddingVector queryEmbedding, String query, int limit);
}
