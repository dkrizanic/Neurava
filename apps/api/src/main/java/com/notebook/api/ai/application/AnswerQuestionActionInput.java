package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerQuestionActionInput(
		@NotBlank(message = "Question is required.")
		@Size(max = 500, message = "Question must be 500 characters or fewer.")
		String question
) {
}
