package com.notebook.api.ai.infrastructure.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.ai.application.MemorySearchMatch;
import com.notebook.api.ai.application.MemorySearchService;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@Validated
@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/search")
class MemorySearchController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final MemorySearchService search;

	MemorySearchController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, MemorySearchService search) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.search = search;
	}

	@GetMapping
	List<MemorySearchMatch> search(Authentication authentication,
			@RequestParam("q")
			@NotBlank(message = "Search query is required.")
			@Size(max = 240, message = "Search query must be 240 characters or fewer.")
			String query) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.search.searchNotes(activeWorkspaceId(accountId), query);
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}
}
