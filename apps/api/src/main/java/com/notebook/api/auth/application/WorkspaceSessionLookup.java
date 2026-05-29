package com.notebook.api.auth.application;

import java.util.UUID;

import com.notebook.api.auth.application.CurrentSession.WorkspaceSummary;

public interface WorkspaceSessionLookup {

	WorkspaceSession activeWorkspaceFor(UUID accountId);

	record WorkspaceSession(
			WorkspaceSummary activeWorkspace,
			boolean workspaceSwitcherAvailable
	) {
	}
}
