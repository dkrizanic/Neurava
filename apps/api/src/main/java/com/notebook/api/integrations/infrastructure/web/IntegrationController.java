package com.notebook.api.integrations.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notebook.api.auth.application.AuthAccountService;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.integrations.application.CalendarEventSummary;
import com.notebook.api.integrations.application.IntegrationService;
import com.notebook.api.integrations.application.IntegrationSummary;
import com.notebook.api.integrations.domain.IntegrationProvider;
import com.notebook.api.shared.infrastructure.web.ApiPaths;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/integrations")
class IntegrationController {

	private final AuthAccountService accounts;
	private final WorkspaceSessionLookup workspaceSessions;
	private final IntegrationService integrations;

	IntegrationController(AuthAccountService accounts, WorkspaceSessionLookup workspaceSessions,
			IntegrationService integrations) {
		this.accounts = accounts;
		this.workspaceSessions = workspaceSessions;
		this.integrations = integrations;
	}

	@GetMapping
	List<IntegrationSummary> list(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.integrations.list(activeWorkspaceId(accountId));
	}

	@PostMapping("/{provider}/connect")
	IntegrationSummary connect(Authentication authentication, @PathVariable IntegrationProvider provider) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.integrations.connect(accountId, activeWorkspaceId(accountId), provider);
	}

	@PostMapping("/{provider}/disconnect")
	IntegrationSummary disconnect(Authentication authentication, @PathVariable IntegrationProvider provider) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.integrations.disconnect(activeWorkspaceId(accountId), provider);
	}

	@GetMapping("/calendar/events")
	List<CalendarEventSummary> calendarEvents(Authentication authentication) {
		UUID accountId = this.accounts.createOrUpdateAccountIdFrom(authentication);
		return this.integrations.upcomingCalendarEvents(activeWorkspaceId(accountId));
	}

	private UUID activeWorkspaceId(UUID accountId) {
		var workspace = this.workspaceSessions.activeWorkspaceFor(accountId).activeWorkspace();
		if (workspace == null) {
			throw new IllegalStateException("Authenticated account does not have an active workspace.");
		}
		return workspace.id();
	}
}

