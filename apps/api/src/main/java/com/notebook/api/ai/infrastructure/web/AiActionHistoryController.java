package com.notebook.api.ai.infrastructure.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.ai.application.AiActionRevertException;
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

	@PostMapping("/{historyId}/revert")
	AiActionHistorySummary revert(Authentication authentication, @PathVariable UUID historyId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.history.revert(historyId, activeWorkspaceId(accountId));
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	@ExceptionHandler(AiActionRevertException.class)
	ResponseEntity<ProblemDetail> handleRevert(AiActionRevertException exception, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("AI action cannot be reverted");
		problem.setInstance(URI.create(request.getRequestURI()));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}
}
