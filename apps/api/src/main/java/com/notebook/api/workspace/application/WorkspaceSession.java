package com.notebook.api.workspace.application;

import java.util.UUID;

import com.notebook.api.workspace.domain.WorkspaceContext;

public record WorkspaceSession(
		WorkspaceSummary activeWorkspace,
		boolean workspaceSwitcherAvailable
) {

	public static WorkspaceSession personal(WorkspaceContext workspace, boolean switcherAvailable) {
		return new WorkspaceSession(
				new WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getType().name()),
				switcherAvailable);
	}

	public record WorkspaceSummary(
			UUID id,
			String name,
			String type
	) {
	}
}
