package com.notebook.api.ai.application;

import java.time.Instant;

public record ReminderChangePreview(
		String title,
		String details,
		Instant dueAt,
		String relatedContext,
		boolean calendarSyncEnabled
) {
}

