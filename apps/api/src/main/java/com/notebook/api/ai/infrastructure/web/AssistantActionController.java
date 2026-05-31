package com.notebook.api.ai.infrastructure.web;

import java.net.URI;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.ai.application.AssistantActionRequest;
import com.notebook.api.ai.application.AssistantActionResponse;
import com.notebook.api.ai.application.AssistantActionService;
import com.notebook.api.ai.application.InvalidAssistantActionInputException;
import com.notebook.api.ai.application.UnsupportedAssistantActionException;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/actions")
class AssistantActionController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final AssistantActionService actions;

	AssistantActionController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			AssistantActionService actions) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.actions = actions;
	}

	@PostMapping
	AssistantActionResponse execute(Authentication authentication, @Valid @RequestBody AssistantActionRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.actions.execute(activeWorkspaceId(accountId), request);
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	@ExceptionHandler(InvalidAssistantActionInputException.class)
	ResponseEntity<ProblemDetail> handleAssistantActionInput(InvalidAssistantActionInputException exception,
			HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed.");
		problem.setTitle("Validation failed");
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("errors", exception.errors());

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(UnsupportedAssistantActionException.class)
	ResponseEntity<ProblemDetail> handleUnsupportedAssistantAction(UnsupportedAssistantActionException exception,
			HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Assistant action is not supported.");
		problem.setTitle("Unsupported assistant action");
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("action", exception.action());

		return ResponseEntity.badRequest().body(problem);
	}
}
