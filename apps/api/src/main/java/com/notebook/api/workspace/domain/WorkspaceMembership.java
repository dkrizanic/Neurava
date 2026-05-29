package com.notebook.api.workspace.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "workspace_membership", uniqueConstraints = {
		@UniqueConstraint(name = "uk_workspace_membership_account_workspace", columnNames = {
				"account_id", "workspace_context_id" })
})
public class WorkspaceMembership {

	@Id
	private UUID id;

	@Column(name = "account_id", nullable = false)
	private UUID accountId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "workspace_context_id", nullable = false)
	private WorkspaceContext workspaceContext;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private WorkspaceRole role;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected WorkspaceMembership() {
	}

	private WorkspaceMembership(UUID accountId, WorkspaceContext workspaceContext, WorkspaceRole role, Instant now) {
		this.id = UUID.randomUUID();
		this.accountId = accountId;
		this.workspaceContext = workspaceContext;
		this.role = role;
		this.createdAt = now;
	}

	public static WorkspaceMembership owner(UUID accountId, WorkspaceContext workspaceContext, Instant now) {
		return new WorkspaceMembership(accountId, workspaceContext, WorkspaceRole.OWNER, now);
	}

	public WorkspaceContext getWorkspaceContext() {
		return this.workspaceContext;
	}
}
