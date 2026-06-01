package com.notebook.api.notes.application;

import java.util.UUID;

public record NoteDeletedEvent(
		UUID noteId,
		UUID workspaceContextId
) {
}
