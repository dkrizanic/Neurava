package com.notebook.api.ai.application;

import java.util.List;

public record SourceAwareAnswer(
		String answer,
		boolean enoughSourceContext,
		List<AnswerSourceReference> sources
) {
}
