package com.notebook.api.ai.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.notebook.api.ai.application.EmbeddingGenerator;
import com.notebook.api.ai.application.EmbeddingVector;
import com.notebook.api.ai.application.NoteRetrievalIndexRecord;
import com.notebook.api.ai.application.NoteRetrievalIndexRepository;
import com.notebook.api.ai.application.NoteRetrievalIndexingService;
import com.notebook.api.ai.domain.RetrievalIndexStatus;
import com.notebook.api.notes.application.NoteContentChangedEvent;

class NoteRetrievalIndexingServiceUnitTest {

	private final EmbeddingGenerator embeddings = mock(EmbeddingGenerator.class);
	private final NoteRetrievalIndexRepository index = mock(NoteRetrievalIndexRepository.class);
	private final NoteRetrievalIndexingService service = new NoteRetrievalIndexingService(this.embeddings, this.index);

	@Test
	void indexesSearchableNoteTextWithWorkspaceScopeAndEmbedding() {
		UUID noteId = UUID.randomUUID();
		UUID workspaceContextId = UUID.randomUUID();
		UUID ownerAccountId = UUID.randomUUID();
		when(this.embeddings.embed(any())).thenReturn(new EmbeddingVector(new double[] {
				0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8,
				0.9, 1.0, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6
		}));

		this.service.index(new NoteContentChangedEvent(
				noteId, workspaceContextId, ownerAccountId, "Index me", "Remember this", Instant.EPOCH));

		ArgumentCaptor<NoteRetrievalIndexRecord> record = ArgumentCaptor.forClass(NoteRetrievalIndexRecord.class);
		verify(this.index).upsert(record.capture());
		assertThat(record.getValue().noteId()).isEqualTo(noteId);
		assertThat(record.getValue().workspaceContextId()).isEqualTo(workspaceContextId);
		assertThat(record.getValue().searchableText()).contains("Index me", "Remember this");
		assertThat(record.getValue().embedding().values()).hasSize(16);
		assertThat(record.getValue().status()).isEqualTo(RetrievalIndexStatus.INDEXED);
	}
}
