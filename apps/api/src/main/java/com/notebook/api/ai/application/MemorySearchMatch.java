package com.notebook.api.ai.application;

import java.time.Instant;
import java.util.UUID;

public record MemorySearchMatch(
		UUID sourceId,
		String sourceType,
		String title,
		String snippet,
		Instant sourceUpdatedAt,
		double score
) {
}
