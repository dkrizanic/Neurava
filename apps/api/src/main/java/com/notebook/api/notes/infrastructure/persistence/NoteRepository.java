package com.notebook.api.notes.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.notes.domain.Note;

public interface NoteRepository extends JpaRepository<Note, UUID> {

	List<Note> findByWorkspaceContextIdOrderByUpdatedAtDesc(UUID workspaceContextId);

	Optional<Note> findByIdAndWorkspaceContextId(UUID id, UUID workspaceContextId);
}
