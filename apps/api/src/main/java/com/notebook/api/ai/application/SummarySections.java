package com.notebook.api.ai.application;

import java.util.List;

public record SummarySections(
		List<String> keyEvents,
		List<String> decisions,
		List<String> unresolvedItems,
		List<String> nextActions
) {
	public static SummarySections insufficientContext() {
		return new SummarySections(
				List.of("Not enough source context is available in this workspace to summarize that topic."),
				List.of(),
				List.of(),
				List.of());
	}
}
