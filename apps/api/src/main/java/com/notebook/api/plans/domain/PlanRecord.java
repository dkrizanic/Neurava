package com.notebook.api.plans.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_record")
public class PlanRecord {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String goal;

	@Column(nullable = false, columnDefinition = "text")
	private String items;

	@Column(name = "linked_resources", nullable = false, length = 1024)
	private String linkedResources;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private PlanStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected PlanRecord() {
	}

	private PlanRecord(UUID ownerAccountId, UUID workspaceContextId, String title, String goal, String items,
			String linkedResources, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.createdAt = now;
		this.status = PlanStatus.ACTIVE;
		update(title, goal, items, linkedResources, now);
	}

	public static PlanRecord create(UUID ownerAccountId, UUID workspaceContextId, String title, String goal,
			String items, String linkedResources, Instant now) {
		return new PlanRecord(ownerAccountId, workspaceContextId, title, goal, items, linkedResources, now);
	}

	public void update(String title, String goal, String items, String linkedResources, Instant now) {
		this.title = title.trim();
		this.goal = goal == null ? "" : goal;
		this.items = items == null ? "" : items;
		this.linkedResources = linkedResources == null ? "" : linkedResources.trim();
		this.updatedAt = now;
	}

	public void setStatus(PlanStatus status, Instant now) {
		this.status = status;
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

	public String getTitle() {
		return this.title;
	}

	public String getGoal() {
		return this.goal;
	}

	public String getItems() {
		return this.items;
	}

	public String getLinkedResources() {
		return this.linkedResources;
	}

	public PlanStatus getStatus() {
		return this.status;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}
}

