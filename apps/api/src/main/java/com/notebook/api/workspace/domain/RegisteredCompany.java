package com.notebook.api.workspace.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "registered_company", uniqueConstraints = {
		@UniqueConstraint(name = "uk_registered_company_owner", columnNames = "owner_account_id"),
		@UniqueConstraint(name = "uk_registered_company_workspace", columnNames = "workspace_context_id")
})
public class RegisteredCompany {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@OneToOne(optional = false)
	@JoinColumn(name = "workspace_context_id", nullable = false)
	private WorkspaceContext workspaceContext;

	@Column(nullable = false, length = 160)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected RegisteredCompany() {
	}

	private RegisteredCompany(UUID ownerAccountId, WorkspaceContext workspaceContext, String name, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContext = workspaceContext;
		this.name = name;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static RegisteredCompany register(UUID ownerAccountId, WorkspaceContext workspaceContext, String name,
			Instant now) {
		return new RegisteredCompany(ownerAccountId, workspaceContext, name.trim(), now);
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getOwnerAccountId() {
		return this.ownerAccountId;
	}

	public String getName() {
		return this.name;
	}

	public WorkspaceContext getWorkspaceContext() {
		return this.workspaceContext;
	}
}
