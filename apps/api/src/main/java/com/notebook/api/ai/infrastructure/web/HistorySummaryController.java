package com.notebook.api.ai.infrastructure.web;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.ai.application.HistorySummary;
import com.notebook.api.ai.application.HistorySummaryService;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/summaries")
class HistorySummaryController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final HistorySummaryService summaries;

	HistorySummaryController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			HistorySummaryService summaries) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.summaries = summaries;
	}

	@PostMapping
	HistorySummary summarize(Authentication authentication, @Valid @RequestBody SummarizeTopicRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.summaries.summarize(activeWorkspaceId(accountId), request.topic());
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record SummarizeTopicRequest(
			@NotBlank(message = "Summary topic is required.")
			@Size(max = 500, message = "Summary topic must be 500 characters or fewer.")
			String topic
	) {
	}
}
