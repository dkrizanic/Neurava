package com.notebook.api.notes.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteSummary;
import com.notebook.api.notes.domain.Note;
import com.notebook.api.notes.infrastructure.persistence.NoteRepository;

class NoteServiceUnitTest {

	private final NoteRepository notes = mock(NoteRepository.class);
	private final NoteService service = new NoteService(this.notes);

	@Test
	void createsNoteWithOwnerAndWorkspaceMetadata() {
		UUID ownerAccountId = UUID.randomUUID();
		UUID workspaceContextId = UUID.randomUUID();
		when(this.notes.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

		NoteSummary note = this.service.create(ownerAccountId, workspaceContextId, "  First note  ", "Body");

		assertThat(note.ownerAccountId()).isEqualTo(ownerAccountId);
		assertThat(note.workspaceContextId()).isEqualTo(workspaceContextId);
		assertThat(note.title()).isEqualTo("First note");
		assertThat(note.body()).isEqualTo("Body");
		assertThat(note.createdAt()).isNotNull();
		assertThat(note.updatedAt()).isNotNull();
	}

	@Test
	void listsNotesFromOneWorkspaceOnly() {
		UUID workspaceContextId = UUID.randomUUID();
		Note newest = Note.create(UUID.randomUUID(), workspaceContextId, "Newest", "", Instant.now());
		Note older = Note.create(UUID.randomUUID(), workspaceContextId, "Older", "", Instant.now());
		when(this.notes.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId)).thenReturn(List.of(newest, older));

		List<NoteSummary> results = this.service.list(workspaceContextId);

		assertThat(results).extracting(NoteSummary::title).containsExactly("Newest", "Older");
	}

	@Test
	void updatesExistingWorkspaceNote() {
		UUID ownerAccountId = UUID.randomUUID();
		UUID workspaceContextId = UUID.randomUUID();
		Note existing = Note.create(ownerAccountId, workspaceContextId, "Before", "Old", Instant.EPOCH);
		when(this.notes.findByIdAndWorkspaceContextId(existing.getId(), workspaceContextId)).thenReturn(Optional.of(existing));

		NoteSummary updated = this.service.update(existing.getId(), workspaceContextId, "After", "New body");

		assertThat(updated.title()).isEqualTo("After");
		assertThat(updated.body()).isEqualTo("New body");
		assertThat(updated.updatedAt()).isAfter(updated.createdAt());
	}
}
