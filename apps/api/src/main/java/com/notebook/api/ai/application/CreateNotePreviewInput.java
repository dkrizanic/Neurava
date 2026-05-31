package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNotePreviewInput(
		@NotBlank(message = "Source text is required.")
		@Size(max = 4000, message = "Source text must be 4000 characters or fewer.")
		String text
) {
}
