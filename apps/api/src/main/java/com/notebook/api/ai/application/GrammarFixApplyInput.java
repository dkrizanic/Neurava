package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrammarFixApplyInput(
		@NotBlank(message = "Note id is required.")
		String noteId,

		@NotBlank(message = "Title is required.")
		@Size(max = 180, message = "Title must be 180 characters or fewer.")
		String title,

		@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
		String body,

		@NotBlank(message = "Proposed body is required.")
		@Size(max = 20000, message = "Proposed body must be 20000 characters or fewer.")
		String proposedBody
) {
}
