package com.notebook.api.ai.application;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.notebook.api.ai.domain.RetrievalIndexStatus;
import com.notebook.api.notes.application.NoteContentChangedEvent;
import com.notebook.api.notes.application.NoteDeletedEvent;

@Service
public class NoteRetrievalIndexingService {

	private final EmbeddingGenerator embeddings;
	private final NoteRetrievalIndexRepository index;

	public NoteRetrievalIndexingService(EmbeddingGenerator embeddings, NoteRetrievalIndexRepository index) {
		this.embeddings = embeddings;
		this.index = index;
	}

	public void index(NoteContentChangedEvent event) {
		String searchableText = searchableText(event);
		EmbeddingVector embedding = this.embeddings.embed(searchableText);
		this.index.upsert(new NoteRetrievalIndexRecord(
				event.noteId(),
				event.workspaceContextId(),
				event.ownerAccountId(),
				event.title(),
				searchableText,
				embedding,
				Instant.now(),
				event.noteUpdatedAt(),
				RetrievalIndexStatus.INDEXED,
				null));
	}

	public void delete(NoteDeletedEvent event) {
		this.index.delete(event.noteId(), event.workspaceContextId());
	}

	private static String searchableText(NoteContentChangedEvent event) {
		return "Title: " + event.title().trim() + "\n\n" + (event.body() == null ? "" : event.body().trim());
	}
}
