package com.notebook.api.integrations.application;

import java.time.Instant;

public record CalendarEventSummary(
		String id,
		String title,
		Instant startsAt,
		Instant endsAt,
		String source
) {
}

