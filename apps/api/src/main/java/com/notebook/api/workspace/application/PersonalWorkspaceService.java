package com.notebook.api.workspace.application;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notebook.api.auth.domain.AuthAccount;
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
	public WorkspaceSession ensurePersonalWorkspace(AuthAccount account) {
		Instant now = Instant.now();
		WorkspaceContext workspace = this.workspaces.findByOwnerAccountAndType(account, WorkspaceContextType.PERSONAL)
				.orElseGet(() -> this.workspaces.save(WorkspaceContext.personalFor(account, now)));

		if (!this.memberships.existsByAccountAndWorkspaceContext(account, workspace)) {
			this.memberships.save(WorkspaceMembership.owner(account, workspace, now));
		}

		return WorkspaceSession.personal(workspace, this.memberships.countByAccount(account) > 1);
	}
}
