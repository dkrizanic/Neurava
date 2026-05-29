package com.notebook.api.workspace.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.notebook.api.auth.application.CurrentSession.WorkspaceSummary;
import com.notebook.api.auth.application.WorkspaceSessionLookup;
import com.notebook.api.workspace.domain.WorkspaceMembership;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceMembershipRepository;

@Component
class AuthWorkspaceSessionLookup implements WorkspaceSessionLookup {

	private final WorkspaceMembershipRepository memberships;

	AuthWorkspaceSessionLookup(WorkspaceMembershipRepository memberships) {
		this.memberships = memberships;
	}

	@Override
	public WorkspaceSession activeWorkspaceFor(UUID accountId) {
		return this.memberships.findFirstByAccountIdOrderByCreatedAtAsc(accountId)
				.map(membership -> toSession(accountId, membership))
				.orElse(new WorkspaceSession(null, false));
	}

	private WorkspaceSession toSession(UUID accountId, WorkspaceMembership membership) {
		var workspace = membership.getWorkspaceContext();
		return new WorkspaceSession(
				new WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getType().name()),
				this.memberships.countByAccountId(accountId) > 1);
	}
}
