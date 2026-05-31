package com.notebook.api.ai.application;

import java.time.Instant;
import java.util.UUID;

public record AnswerSourceReference(
		UUID id,
		String type,
		String title,
		String snippet,
		Instant sourceUpdatedAt,
		double score
) {
	static AnswerSourceReference from(MemorySearchMatch match) {
		return new AnswerSourceReference(
				match.sourceId(),
				match.sourceType(),
				match.title(),
				match.snippet(),
				match.sourceUpdatedAt(),
				match.score());
	}
}
