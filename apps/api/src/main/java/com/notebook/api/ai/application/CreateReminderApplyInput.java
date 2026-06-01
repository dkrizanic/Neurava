package com.notebook.api.ai.application;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReminderApplyInput(
		@NotBlank(message = "Title is required.")
		@Size(max = 180, message = "Title must be 180 characters or fewer.")
		String title,
		@Size(max = 20000, message = "Details must be 20000 characters or fewer.")
		String details,
		@NotNull(message = "Due date is required.")
		Instant dueAt,
		@Size(max = 512, message = "Related context must be 512 characters or fewer.")
		String relatedContext,
		boolean calendarSyncEnabled
) {
}

