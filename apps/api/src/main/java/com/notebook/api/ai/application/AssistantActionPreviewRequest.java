package com.notebook.api.ai.application;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssistantActionPreviewRequest(
		@NotBlank(message = "Preview action is required.")
		@Size(max = 64, message = "Preview action must be 64 characters or fewer.")
		String action,

		@NotNull(message = "Preview input is required.")
		Map<String, Object> input
) {
}
