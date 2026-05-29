package com.notebook.api.memory.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.notebook.api.notes.application.NoteContentChangedEvent;

@Component
public class NoteRetrievalIndexListener {

	private static final Logger log = LoggerFactory.getLogger(NoteRetrievalIndexListener.class);

	private final NoteRetrievalIndexingService indexing;

	public NoteRetrievalIndexListener(NoteRetrievalIndexingService indexing) {
		this.indexing = indexing;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void on(NoteContentChangedEvent event) {
		try {
			this.indexing.index(event);
		} catch (RuntimeException exception) {
			log.warn("Note retrieval indexing failed for noteId={} workspaceContextId={} cause={}: {}",
					event.noteId(),
					event.workspaceContextId(),
					exception.getClass().getSimpleName(),
					exception.getMessage());
		}
	}
}
