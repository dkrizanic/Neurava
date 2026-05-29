package com.notebook.api.workspace.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceContextType;
import com.notebook.api.workspace.domain.WorkspaceMembership;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceContextRepository;
import com.notebook.api.workspace.infrastructure.persistence.WorkspaceMembershipRepository;

@Service
public class PersonalWorkspaceService {

	private final WorkspaceContextRepository workspaces;
	private final WorkspaceMembershipRepository memberships;

	public PersonalWorkspaceService(WorkspaceContextRepository workspaces, WorkspaceMembershipRepository memberships) {
		this.workspaces = workspaces;
		this.memberships = memberships;
	}

	@Transactional
	public WorkspaceSession ensurePersonalWorkspace(UUID accountId) {
		Instant now = Instant.now();
		WorkspaceContext workspace = this.workspaces.findByOwnerAccountIdAndType(accountId, WorkspaceContextType.PERSONAL)
				.orElseGet(() -> this.workspaces.save(WorkspaceContext.personalFor(accountId, now)));

		if (!this.memberships.existsByAccountIdAndWorkspaceContext(accountId, workspace)) {
			this.memberships.save(WorkspaceMembership.owner(accountId, workspace, now));
		}

		return WorkspaceSession.personal(workspace, this.memberships.countByAccountId(accountId) > 1);
	}
}
