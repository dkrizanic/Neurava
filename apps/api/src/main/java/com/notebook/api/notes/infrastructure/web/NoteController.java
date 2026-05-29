package com.notebook.api.notes.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.notes.application.NoteService;
import com.notebook.api.notes.application.NoteSummary;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/notes")
class NoteController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final NoteService notes;

	NoteController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, NoteService notes) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.notes = notes;
	}

	@GetMapping
	List<NoteSummary> list(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.list(workspaceId);
	}

	@PostMapping
	NoteSummary create(Authentication authentication, @Valid @RequestBody CreateNoteRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.create(accountId, workspaceId, request.title(), request.body());
	}

	@PatchMapping("/{noteId}")
	NoteSummary update(Authentication authentication, @PathVariable UUID noteId,
			@Valid @RequestBody UpdateNoteRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		UUID workspaceId = activeWorkspaceId(accountId);
		return this.notes.update(noteId, workspaceId, request.title(), request.body());
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record CreateNoteRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
			String body
	) {
	}

	record UpdateNoteRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Body must be 20000 characters or fewer.")
			String body
	) {
	}
}
