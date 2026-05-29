package com.notebook.api.notes.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "note")
public class Note {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String body;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Note() {
	}

	private Note(UUID ownerAccountId, UUID workspaceContextId, String title, String body, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.title = title.trim();
		this.body = body == null ? "" : body;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static Note create(UUID ownerAccountId, UUID workspaceContextId, String title, String body, Instant now) {
		return new Note(ownerAccountId, workspaceContextId, title, body, now);
	}

	public void update(String title, String body, Instant now) {
		this.title = title.trim();
		this.body = body == null ? "" : body;
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

	public String getBody() {
		return this.body;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}
}
