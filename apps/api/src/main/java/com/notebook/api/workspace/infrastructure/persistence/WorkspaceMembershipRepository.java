package com.notebook.api.workspace.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notebook.api.workspace.domain.WorkspaceContext;
import com.notebook.api.workspace.domain.WorkspaceMembership;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

	boolean existsByAccountIdAndWorkspaceContext(UUID accountId, WorkspaceContext workspaceContext);

	long countByAccountId(UUID accountId);

	Optional<WorkspaceMembership> findFirstByAccountIdOrderByCreatedAtAsc(UUID accountId);
}
