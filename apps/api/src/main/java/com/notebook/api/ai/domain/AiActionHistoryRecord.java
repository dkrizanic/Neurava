package com.notebook.api.ai.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_action_history")
public class AiActionHistoryRecord {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Column(nullable = false, length = 80)
	private String action;

	@Column(name = "entity_type", nullable = false, length = 80)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;

	@Column(name = "change_type", nullable = false, length = 40)
	private String changeType;

	@Column(nullable = false, length = 512)
	private String summary;

	@Column(name = "previous_state", columnDefinition = "text")
	private String previousState;

	@Column(name = "current_state", nullable = false, columnDefinition = "text")
	private String currentState;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "reverted_at")
	private Instant revertedAt;

	@Column(name = "revert_summary", length = 512)
	private String revertSummary;

	protected AiActionHistoryRecord() {
	}

	private AiActionHistoryRecord(UUID ownerAccountId, UUID workspaceContextId, String action, String entityType,
			UUID entityId, String changeType, String summary, String previousState, String currentState, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.action = action;
		this.entityType = entityType;
		this.entityId = entityId;
		this.changeType = changeType;
		this.summary = summary;
		this.previousState = previousState;
		this.currentState = currentState;
		this.createdAt = now;
	}

	public static AiActionHistoryRecord create(UUID ownerAccountId, UUID workspaceContextId, String action,
			String entityType, UUID entityId, String changeType, String summary, String previousState,
			String currentState, Instant now) {
		return new AiActionHistoryRecord(ownerAccountId, workspaceContextId, action, entityType, entityId,
				changeType, summary, previousState, currentState, now);
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

	public String getAction() {
		return this.action;
	}

	public String getEntityType() {
		return this.entityType;
	}

	public UUID getEntityId() {
		return this.entityId;
	}

	public String getChangeType() {
		return this.changeType;
	}

	public String getSummary() {
		return this.summary;
	}

	public String getPreviousState() {
		return this.previousState;
	}

	public String getCurrentState() {
		return this.currentState;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getRevertedAt() {
		return this.revertedAt;
	}

	public String getRevertSummary() {
		return this.revertSummary;
	}

	public boolean isReverted() {
		return this.revertedAt != null;
	}

	public void markReverted(String revertSummary, Instant now) {
		this.revertedAt = now;
		this.revertSummary = revertSummary;
	}
}
