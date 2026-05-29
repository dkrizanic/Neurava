package com.notebook.api.notes.application;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.notebook.api.notes.domain.Note;
import com.notebook.api.notes.domain.EditorMode;
import com.notebook.api.notes.infrastructure.persistence.NoteRepository;

@Service
public class NoteService {

	private final NoteRepository notes;

	public NoteService(NoteRepository notes) {
		this.notes = notes;
	}

	@Transactional
	public NoteSummary create(UUID ownerAccountId, UUID workspaceContextId, String title, String body) {
		Note note = Note.create(ownerAccountId, workspaceContextId, title, body, Instant.now());
		return NoteSummary.from(this.notes.save(note));
	}

	@Transactional(readOnly = true)
	public List<NoteSummary> list(UUID workspaceContextId, NoteFilters filters) {
		return this.notes.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId).stream()
				.filter(note -> filters.matches(note))
				.map(NoteSummary::from)
				.toList();
	}

	@Transactional
	public NoteSummary update(UUID noteId, UUID workspaceContextId, String title, String body) {
		Note note = this.notes.findByIdAndWorkspaceContextId(noteId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found."));
		note.update(title, body, Instant.now());
		return NoteSummary.from(note);
	}

	@Transactional
	public NoteSummary organize(UUID noteId, UUID workspaceContextId, String tags, boolean favorite, boolean pinned,
			EditorMode editorMode, String linkedResources) {
		Note note = findWorkspaceNote(noteId, workspaceContextId);
		note.organize(tags, favorite, pinned, editorMode, linkedResources, Instant.now());
		return NoteSummary.from(note);
	}

	@Transactional
	public NoteSummary archive(UUID noteId, UUID workspaceContextId) {
		Note note = findWorkspaceNote(noteId, workspaceContextId);
		note.archive(Instant.now());
		return NoteSummary.from(note);
	}

	@Transactional
	public NoteSummary restore(UUID noteId, UUID workspaceContextId) {
		Note note = findWorkspaceNote(noteId, workspaceContextId);
		note.restore(Instant.now());
		return NoteSummary.from(note);
	}

	private Note findWorkspaceNote(UUID noteId, UUID workspaceContextId) {
		return this.notes.findByIdAndWorkspaceContextId(noteId, workspaceContextId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found."));
	}

	public record NoteFilters(
			String query,
			String tag,
			Boolean favorite,
			Boolean pinned,
			boolean archived
	) {

		public boolean matches(Note note) {
			return archivedStateMatches(note)
					&& queryMatches(note)
					&& tagMatches(note)
					&& flagMatches(this.favorite, note.isFavorite())
					&& flagMatches(this.pinned, note.isPinned());
		}

		private boolean archivedStateMatches(Note note) {
			return this.archived == (note.getArchivedAt() != null);
		}

		private boolean queryMatches(Note note) {
			if (this.query == null || this.query.isBlank()) {
				return true;
			}

			String normalized = this.query.toLowerCase(Locale.ROOT);
			return note.getTitle().toLowerCase(Locale.ROOT).contains(normalized)
					|| note.getBody().toLowerCase(Locale.ROOT).contains(normalized);
		}

		private boolean tagMatches(Note note) {
			return this.tag == null || this.tag.isBlank()
					|| note.getTags().toLowerCase(Locale.ROOT).contains(this.tag.toLowerCase(Locale.ROOT));
		}

		private static boolean flagMatches(Boolean expected, boolean actual) {
			return expected == null || expected == actual;
		}
	}
}
