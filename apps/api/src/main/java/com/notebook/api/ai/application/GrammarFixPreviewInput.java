package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GrammarFixPreviewInput(
		@NotBlank(message = "Note id is required.")
		String noteId,

		@Size(max = 180, message = "Title must be 180 characters or fewer.")
		String title,

		@NotBlank(message = "Body is required.")
		@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
		String body
) {
}
