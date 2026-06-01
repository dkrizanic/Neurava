package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlanApplyInput(
		@NotBlank(message = "Title is required.")
		@Size(max = 180, message = "Title must be 180 characters or fewer.")
		String title,
		@Size(max = 20000, message = "Goal must be 20000 characters or fewer.")
		String goal,
		@Size(max = 20000, message = "Items must be 20000 characters or fewer.")
		String items,
		@Size(max = 1024, message = "Linked resources must be 1024 characters or fewer.")
		String linkedResources
) {
}

