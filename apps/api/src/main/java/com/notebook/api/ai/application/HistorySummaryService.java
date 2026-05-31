package com.notebook.api.ai.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class HistorySummaryService {

	private final MemorySearchService search;

	public HistorySummaryService(MemorySearchService search) {
		this.search = search;
	}

	public HistorySummary summarize(UUID workspaceContextId, String topic) {
		List<MemorySearchMatch> matches = this.search.searchNotes(workspaceContextId, topic).stream()
				.limit(5)
				.toList();

		if (matches.isEmpty()) {
			return new HistorySummary(false, SummarySections.insufficientContext(), List.of());
		}

		return new HistorySummary(
				true,
				new SummarySections(
						keyEvents(matches),
						decisions(matches),
						unresolvedItems(matches),
						nextActions(matches)),
				matches.stream().map(AnswerSourceReference::from).toList());
	}

	private static List<String> keyEvents(List<MemorySearchMatch> matches) {
		return matches.stream()
				.map(match -> "From \"%s\": %s".formatted(match.title(), match.snippet()))
				.toList();
	}

	private static List<String> decisions(List<MemorySearchMatch> matches) {
		List<String> decisions = matches.stream()
				.filter(match -> containsAny(match.snippet(), "decided", "decision", "agreed", "chose"))
				.map(match -> "Decision signal in \"%s\": %s".formatted(match.title(), match.snippet()))
				.toList();
		if (!decisions.isEmpty()) {
			return decisions;
		}
		return List.of("No explicit decisions were found in the retrieved note sources.");
	}

	private static List<String> unresolvedItems(List<MemorySearchMatch> matches) {
		List<String> unresolved = matches.stream()
				.filter(match -> containsAny(match.snippet(), "todo", "open", "unresolved", "blocked", "risk", "question"))
				.map(match -> "Open item signal in \"%s\": %s".formatted(match.title(), match.snippet()))
				.toList();
		if (!unresolved.isEmpty()) {
			return unresolved;
		}
		return List.of("No explicit unresolved items were found in the retrieved note sources.");
	}

	private static List<String> nextActions(List<MemorySearchMatch> matches) {
		List<String> actions = matches.stream()
				.filter(match -> containsAny(match.snippet(), "next", "follow", "action", "plan", "schedule"))
				.map(match -> "Next-action signal in \"%s\": %s".formatted(match.title(), match.snippet()))
				.toList();
		if (!actions.isEmpty()) {
			return actions;
		}
		return List.of("No explicit next actions were found in the retrieved note sources.");
	}

	private static boolean containsAny(String text, String... terms) {
		String normalized = text.toLowerCase(Locale.ROOT);
		for (String term : terms) {
			if (normalized.contains(term)) {
				return true;
			}
		}
		return false;
	}
}
