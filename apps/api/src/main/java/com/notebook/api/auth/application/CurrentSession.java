package com.notebook.api.auth.application;

import java.util.UUID;

import com.notebook.api.workspace.application.WorkspaceSession.WorkspaceSummary;

public record CurrentSession(
		boolean authenticated,
		AccountSummary account,
		WorkspaceSummary activeWorkspace,
		boolean workspaceSwitcherAvailable
) {

	public static CurrentSession anonymous() {
		return new CurrentSession(false, null, null, false);
	}

	public static CurrentSession authenticated(AccountSummary account, WorkspaceSummary activeWorkspace,
			boolean workspaceSwitcherAvailable) {
		return new CurrentSession(true, account, activeWorkspace, workspaceSwitcherAvailable);
	}

	public record AccountSummary(
			UUID id,
			String email,
			String displayName,
			String avatarUrl
	) {
	}
}
