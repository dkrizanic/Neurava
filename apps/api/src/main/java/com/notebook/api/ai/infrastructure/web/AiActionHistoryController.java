package com.notebook.api.ai.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.ai.application.AiActionHistoryService;
import com.notebook.api.ai.application.AiActionHistorySummary;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/action-history")
class AiActionHistoryController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final AiActionHistoryService history;

	AiActionHistoryController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			AiActionHistoryService history) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.history = history;
	}

	@GetMapping
	List<AiActionHistorySummary> list(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.history.list(activeWorkspaceId(accountId));
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}
}
