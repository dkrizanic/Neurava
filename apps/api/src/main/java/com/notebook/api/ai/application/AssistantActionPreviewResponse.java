package com.notebook.api.ai.application;

public record AssistantActionPreviewResponse(
		String action,
		String entityType,
		String changeType,
		String summary,
		Object preview
) {
}
