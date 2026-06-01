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

import com.notebook.api.ai.application.AssistantActionApplicationRequest;
import com.notebook.api.ai.application.AssistantActionApplicationResponse;
import com.notebook.api.ai.application.AssistantActionApplicationService;
import com.notebook.api.ai.application.InvalidAssistantActionInputException;
import com.notebook.api.ai.application.UnsupportedAssistantActionApplicationException;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/action-applications")
class AssistantActionApplicationController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final AssistantActionApplicationService applications;

	AssistantActionApplicationController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			AssistantActionApplicationService applications) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.applications = applications;
	}

	@PostMapping
	AssistantActionApplicationResponse apply(Authentication authentication,
			@Valid @RequestBody AssistantActionApplicationRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.applications.apply(accountId, activeWorkspaceId(accountId), request);
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
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Assistant action application input is invalid.");
		problem.setTitle("Validation failed");
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("errors", exception.errors());
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(UnsupportedAssistantActionApplicationException.class)
	ResponseEntity<ProblemDetail> handleUnsupportedAssistantActionApplication(
			UnsupportedAssistantActionApplicationException exception, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Assistant action application is not supported.");
		problem.setTitle("Unsupported assistant action application");
		problem.setInstance(URI.create(request.getRequestURI()));
		problem.setProperty("action", exception.action());
		return ResponseEntity.badRequest().body(problem);
	}
}
