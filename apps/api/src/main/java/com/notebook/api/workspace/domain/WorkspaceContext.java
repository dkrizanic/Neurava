package com.notebook.api.workspace.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "workspace_context", uniqueConstraints = {
		@UniqueConstraint(name = "uk_workspace_context_owner_type", columnNames = { "owner_account_id", "type" })
})
public class WorkspaceContext {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private WorkspaceContextType type;

	@Column(nullable = false)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected WorkspaceContext() {
	}

	private WorkspaceContext(UUID ownerAccountId, WorkspaceContextType type, String name, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.type = type;
		this.name = name;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static WorkspaceContext personalFor(UUID accountId, Instant now) {
		return new WorkspaceContext(accountId, WorkspaceContextType.PERSONAL, "Personal", now);
	}

	public static WorkspaceContext businessFor(UUID accountId, String name, Instant now) {
		return new WorkspaceContext(accountId, WorkspaceContextType.BUSINESS, name.trim(), now);
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getOwnerAccountId() {
		return this.ownerAccountId;
	}

	public WorkspaceContextType getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}
}
