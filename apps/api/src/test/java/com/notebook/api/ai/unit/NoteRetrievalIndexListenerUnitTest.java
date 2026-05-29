package com.notebook.api.ai.unit;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.notebook.api.ai.application.NoteRetrievalIndexListener;
import com.notebook.api.ai.application.NoteRetrievalIndexingService;
import com.notebook.api.notes.application.NoteContentChangedEvent;

class NoteRetrievalIndexListenerUnitTest {

	private final NoteRetrievalIndexingService indexing = mock(NoteRetrievalIndexingService.class);
	private final NoteRetrievalIndexListener listener = new NoteRetrievalIndexListener(this.indexing);

	@Test
	void indexesNoteContentChangedEvent() {
		NoteContentChangedEvent event = event();

		this.listener.on(event);

		verify(this.indexing).index(event);
	}

	@Test
	void indexingFailureDoesNotEscapeListener() {
		NoteContentChangedEvent event = event();
		doThrow(new IllegalStateException("provider down")).when(this.indexing).index(event);

		this.listener.on(event);
	}

	private static NoteContentChangedEvent event() {
		return new NoteContentChangedEvent(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Title", "Body", Instant.EPOCH);
	}
}
