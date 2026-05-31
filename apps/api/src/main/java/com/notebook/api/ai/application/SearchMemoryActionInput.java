package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchMemoryActionInput(
		@NotBlank(message = "Search query is required.")
		@Size(max = 240, message = "Search query must be 240 characters or fewer.")
		String query
) {
}
