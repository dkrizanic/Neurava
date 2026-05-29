package com.notebook.api.notes.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.notebook.api.notes.domain.Note;
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
	public List<NoteSummary> list(UUID workspaceContextId) {
		return this.notes.findByWorkspaceContextIdOrderByUpdatedAtDesc(workspaceContextId).stream()
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
}
