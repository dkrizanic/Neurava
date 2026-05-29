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
import com.notebook.api.notes.application.NoteService.NoteFilters;
import com.notebook.api.notes.application.NoteSummary;
import com.notebook.api.notes.domain.EditorMode;
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

		List<NoteSummary> results = this.service.list(workspaceContextId, new NoteFilters(null, null, null, null, false));

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

	@Test
	void archivesRestoresAndFiltersNotes() {
		UUID workspaceContextId = UUID.randomUUID();
		Note active = Note.create(UUID.randomUUID(), workspaceContextId, "Active", "Search body", Instant.EPOCH);
		Note archived = Note.create(UUID.randomUUID(), workspaceContextId, "Archived", "", Instant.EPOCH);
		archived.archive(Instant.now());
		when(this.notes.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId)).thenReturn(List.of(archived, active));
		when(this.notes.findByIdAndWorkspaceContextId(active.getId(), workspaceContextId)).thenReturn(Optional.of(active));

		NoteSummary organized = this.service.organize(active.getId(), workspaceContextId, "planning,work", true, true,
				EditorMode.MARKDOWN, "PLAN:123");

		assertThat(organized.tags()).isEqualTo("planning,work");
		assertThat(organized.favorite()).isTrue();
		assertThat(organized.pinned()).isTrue();
		assertThat(organized.editorMode()).isEqualTo("MARKDOWN");
		assertThat(this.service.list(workspaceContextId, new NoteFilters("search", "planning", true, true, false)))
				.extracting(NoteSummary::title)
				.containsExactly("Active");
		assertThat(this.service.list(workspaceContextId, new NoteFilters(null, null, null, null, true)))
				.extracting(NoteSummary::title)
				.containsExactly("Archived");
	}
}
