package com.notebook.api.ai.application;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssistantActionApplicationRequest(
		@NotBlank(message = "Action is required.")
		String action,
		@NotNull(message = "Input is required.")
		Map<String, Object> input
) {
}
