package com.notebook.api.notes.application;

import java.time.Instant;
import java.util.UUID;

import com.notebook.api.notes.domain.Note;

public record NoteSummary(
		UUID id,
		UUID ownerAccountId,
		UUID workspaceContextId,
		String title,
		String body,
		Instant createdAt,
		Instant updatedAt
) {

	public static NoteSummary from(Note note) {
		return new NoteSummary(
				note.getId(),
				note.getOwnerAccountId(),
				note.getWorkspaceContextId(),
				note.getTitle(),
				note.getBody(),
				note.getCreatedAt(),
				note.getUpdatedAt());
	}
}
