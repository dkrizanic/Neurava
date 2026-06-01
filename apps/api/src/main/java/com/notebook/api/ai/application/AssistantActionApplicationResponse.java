package com.notebook.api.ai.application;

import com.notebook.api.notes.application.NoteSummary;

public record AssistantActionApplicationResponse(
		String action,
		String entityType,
		String changeType,
		String summary,
		NoteSummary entity
) {
}
