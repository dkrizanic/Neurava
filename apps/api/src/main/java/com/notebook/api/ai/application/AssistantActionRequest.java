package com.notebook.api.ai.application;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssistantActionRequest(
		@NotBlank(message = "Action is required.")
		@Size(max = 64, message = "Action must be 64 characters or fewer.")
		String action,

		@NotNull(message = "Action input is required.")
		Map<String, Object> input
) {
}
