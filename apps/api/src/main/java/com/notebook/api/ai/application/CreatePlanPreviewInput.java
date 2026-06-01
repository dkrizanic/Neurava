package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlanPreviewInput(
		@NotBlank(message = "Goal is required.")
		@Size(max = 20000, message = "Goal must be 20000 characters or fewer.")
		String goal
) {
}

