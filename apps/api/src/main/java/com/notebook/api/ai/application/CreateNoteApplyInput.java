package com.notebook.api.ai.application;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteApplyInput(
		@NotBlank(message = "Title is required.")
		@Size(max = 180, message = "Title must be 180 characters or fewer.")
		String title,
		@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
		String body,
		@Size(max = 512, message = "Tags must be 512 characters or fewer.")
		String tags,
		LocalDate noteDate
) {
	public CreateNoteApplyInput {
		if (noteDate == null) {
			noteDate = LocalDate.now();
		}
	}
}
