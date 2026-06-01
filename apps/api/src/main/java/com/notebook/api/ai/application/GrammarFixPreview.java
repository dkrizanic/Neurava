package com.notebook.api.ai.application;

public record GrammarFixPreview(
		String noteId,
		String currentBody,
		String proposedBody
) {
}
