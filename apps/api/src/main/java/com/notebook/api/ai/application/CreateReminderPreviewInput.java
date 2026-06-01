package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReminderPreviewInput(
		@NotBlank(message = "Text is required.")
		@Size(max = 20000, message = "Text must be 20000 characters or fewer.")
		String text
) {
}

