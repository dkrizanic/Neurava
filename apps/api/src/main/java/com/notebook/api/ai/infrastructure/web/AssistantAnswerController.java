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

import com.notebook.api.ai.application.SourceAwareAnswer;
import com.notebook.api.ai.application.SourceAwareAnswerService;
import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/ai/answers")
class AssistantAnswerController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final SourceAwareAnswerService answers;

	AssistantAnswerController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			SourceAwareAnswerService answers) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.answers = answers;
	}

	@PostMapping
	SourceAwareAnswer answer(Authentication authentication, @Valid @RequestBody AnswerQuestionRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.answers.answer(activeWorkspaceId(accountId), request.question());
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record AnswerQuestionRequest(
			@NotBlank(message = "Question is required.")
			@Size(max = 500, message = "Question must be 500 characters or fewer.")
			String question
	) {
	}
}
