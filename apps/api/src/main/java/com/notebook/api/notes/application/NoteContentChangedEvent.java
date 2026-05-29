package com.notebook.api.notes.application;

import java.time.Instant;
import java.util.UUID;

public record NoteContentChangedEvent(
		UUID noteId,
		UUID workspaceContextId,
		UUID ownerAccountId,
		String title,
		String body,
		Instant noteUpdatedAt
) {
}
