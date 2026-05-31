package com.notebook.api.ai.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SourceAwareAnswerService {

	private final MemorySearchService search;

	public SourceAwareAnswerService(MemorySearchService search) {
		this.search = search;
	}

	public SourceAwareAnswer answer(UUID workspaceContextId, String question) {
		List<MemorySearchMatch> matches = this.search.searchNotes(workspaceContextId, question).stream()
				.limit(3)
				.toList();

		if (matches.isEmpty()) {
			return new SourceAwareAnswer(
					"I do not have enough source context in this workspace to answer that yet.",
					false,
					List.of());
		}

		String answer = "Based on the available notebook sources, " + synthesis(matches) +
				" Open the sources below to inspect the original notes.";
		return new SourceAwareAnswer(answer, true, matches.stream().map(AnswerSourceReference::from).toList());
	}

	private static String synthesis(List<MemorySearchMatch> matches) {
		if (matches.size() == 1) {
			return "the strongest matching note is \"%s\", which says: %s"
					.formatted(matches.getFirst().title(), matches.getFirst().snippet());
		}

		return "the strongest matching notes point to: " + matches.stream()
				.map(match -> "\"%s\": %s".formatted(match.title(), match.snippet()))
				.reduce((left, right) -> left + "; " + right)
				.orElse("");
	}
}
