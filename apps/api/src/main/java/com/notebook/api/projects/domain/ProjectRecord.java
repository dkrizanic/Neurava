package com.notebook.api.projects.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_record")
public class ProjectRecord {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Column(nullable = false, length = 180)
	private String name;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ProjectStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ProjectRecord() {
	}

	private ProjectRecord(UUID ownerAccountId, UUID workspaceContextId, String name, String description, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.createdAt = now;
		this.status = ProjectStatus.ACTIVE;
		update(name, description, this.status, now);
	}

	public static ProjectRecord create(UUID ownerAccountId, UUID workspaceContextId, String name, String description,
			Instant now) {
		return new ProjectRecord(ownerAccountId, workspaceContextId, name, description, now);
	}

	public void update(String name, String description, ProjectStatus status, Instant now) {
		this.name = name.trim();
		this.description = description == null ? "" : description;
		this.status = status == null ? ProjectStatus.ACTIVE : status;
		this.updatedAt = now;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getOwnerAccountId() {
		return this.ownerAccountId;
	}

	public UUID getWorkspaceContextId() {
		return this.workspaceContextId;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public ProjectStatus getStatus() {
		return this.status;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}
}

