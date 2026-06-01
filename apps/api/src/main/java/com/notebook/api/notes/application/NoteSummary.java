package com.notebook.api.notes.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.notebook.api.notes.domain.Note;

public record NoteSummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String title,
		String body,
		Instant createdAt,
		Instant updatedAt,
		LocalDate noteDate,
		Instant archivedAt,
		String tags,
		boolean favorite,
		boolean pinned,
		String editorMode,
		String linkedResources
) {

	public static NoteSummary from(Note note) {
		return new NoteSummary(
				note.getId(),
				note.getOwnerAccountId(),
				note.getWorkspaceContextId(),
				note.getTitle(),
				note.getBody(),
				note.getCreatedAt(),
				note.getUpdatedAt(),
				note.getNoteDate(),
				note.getArchivedAt(),
				note.getTags(),
				note.isFavorite(),
				note.isPinned(),
				note.getEditorMode(),
				note.getLinkedResources());
	}
}
