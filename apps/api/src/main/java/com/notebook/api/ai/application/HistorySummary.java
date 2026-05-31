package com.notebook.api.ai.application;

import java.util.List;

public record HistorySummary(
		boolean enoughSourceContext,
		SummarySections sections,
		List<AnswerSourceReference> sources
) {
}
