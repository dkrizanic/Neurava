package com.notebook.api.ai.application;

public record AssistantActionApplicationResponse(
		String action,
		String entityType,
		String changeType,
		String summary,
		Object entity
) {
}
