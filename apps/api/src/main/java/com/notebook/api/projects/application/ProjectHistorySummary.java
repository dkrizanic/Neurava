package com.notebook.api.projects.application;

import java.util.List;

import com.notebook.api.notes.application.NoteSummary;

public record ProjectHistorySummary(
		ProjectSummary project,
		List<String> timeline,
		List<String> decisions,
		List<String> unresolvedItems,
		List<String> nextActions,
		List<NoteSummary> sources
) {
}

