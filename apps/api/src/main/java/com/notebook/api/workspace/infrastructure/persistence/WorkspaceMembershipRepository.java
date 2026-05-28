package com.notebook.api.workspace.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.auth.domain.AuthAccount;
import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceMembership;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

	boolean existsByAccountAndWorkspaceContext(AuthAccount account, WorkspaceContext workspaceContext);

	long countByAccount(AuthAccount account);
}
