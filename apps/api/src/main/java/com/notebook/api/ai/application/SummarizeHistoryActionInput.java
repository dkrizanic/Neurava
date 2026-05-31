package com.notebook.api.ai.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SummarizeHistoryActionInput(
		@NotBlank(message = "Summary topic is required.")
		@Size(max = 500, message = "Summary topic must be 500 characters or fewer.")
		String topic
) {
}
