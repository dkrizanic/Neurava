package com.notebook.api.reminders.infrastructure.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.reminders.application.ReminderService;
import com.notebook.api.reminders.application.ReminderSummary;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/reminders")
class ReminderController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final ReminderService reminders;

	ReminderController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions, ReminderService reminders) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.reminders = reminders;
	}

	@GetMapping
	List<ReminderSummary> list(Authentication authentication,
			@RequestParam(defaultValue = "false") boolean includeCompleted) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.reminders.list(activeWorkspaceId(accountId), includeCompleted);
	}

	@PostMapping
	ReminderSummary create(Authentication authentication, @Valid @RequestBody UpsertReminderRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.reminders.create(accountId, activeWorkspaceId(accountId), request.title(), request.details(),
				request.dueAt(), request.relatedContext(), request.calendarSyncEnabled());
	}

	@PatchMapping("/{reminderId}")
	ReminderSummary update(Authentication authentication, @PathVariable UUID reminderId,
			@Valid @RequestBody UpsertReminderRequest request) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.reminders.update(reminderId, activeWorkspaceId(accountId), request.title(), request.details(),
				request.dueAt(), request.relatedContext(), request.calendarSyncEnabled());
	}

	@PatchMapping("/{reminderId}/complete")
	ReminderSummary complete(Authentication authentication, @PathVariable UUID reminderId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.reminders.complete(reminderId, activeWorkspaceId(accountId));
	}

	@PatchMapping("/{reminderId}/reopen")
	ReminderSummary reopen(Authentication authentication, @PathVariable UUID reminderId) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.reminders.reopen(reminderId, activeWorkspaceId(accountId));
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}

	record UpsertReminderRequest(
			@NotBlank(message = "Title is required.")
			@Size(max = 180, message = "Title must be 180 characters or fewer.")
			String title,
			@Size(max = 20000, message = "Details must be 20000 characters or fewer.")
			String details,
			@NotNull(message = "Due date is required.")
			Instant dueAt,
			@Size(max = 512, message = "Related context must be 512 characters or fewer.")
			String relatedContext,
			boolean calendarSyncEnabled
	) {
	}
}

