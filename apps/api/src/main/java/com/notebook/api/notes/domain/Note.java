package com.notebook.api.notes.domain;

import java.time.Instant;
import java.time.LocalDate;
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

	@Column(name = "note_date", nullable = false)
	private LocalDate noteDate;

	@Column(name = "archived_at")
	private Instant archivedAt;

	@Column(nullable = false, length = 512)
	private String tags = "";

	@Column(nullable = false)
	private boolean favorite;

	@Column(nullable = false)
	private boolean pinned;

	@Column(name = "editor_mode", nullable = false, length = 40)
	private String editorMode = EditorMode.RICH_TEXT.name();

	@Column(name = "linked_resources", nullable = false, length = 1024)
	private String linkedResources = "";

	protected Note() {
	}

	private Note(UUID ownerAccountId, UUID workspaceContextId, String title, String body, LocalDate noteDate,
			Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.title = title.trim();
		this.body = body == null ? "" : body;
		this.noteDate = noteDate;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public static Note create(UUID ownerAccountId, UUID workspaceContextId, String title, String body, LocalDate noteDate,
			Instant now) {
		return new Note(ownerAccountId, workspaceContextId, title, body, noteDate, now);
	}

	public void update(String title, String body, Instant now) {
		this.title = title.trim();
		this.body = body == null ? "" : body;
		this.updatedAt = now;
	}

	public void organize(String tags, boolean favorite, boolean pinned, EditorMode editorMode, String linkedResources,
			Instant now) {
		this.tags = tags == null ? "" : tags.trim();
		this.favorite = favorite;
		this.pinned = pinned;
		this.editorMode = editorMode.name();
		this.linkedResources = linkedResources == null ? "" : linkedResources.trim();
		this.updatedAt = now;
	}

	public void archive(Instant now) {
		this.archivedAt = now;
		this.updatedAt = now;
	}

	public void restore(Instant now) {
		this.archivedAt = null;
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

	public LocalDate getNoteDate() {
		return this.noteDate;
	}

	public Instant getArchivedAt() {
		return this.archivedAt;
	}

	public String getTags() {
		return this.tags;
	}

	public boolean isFavorite() {
		return this.favorite;
	}

	public boolean isPinned() {
		return this.pinned;
	}

	public String getEditorMode() {
		return this.editorMode;
	}

	public String getLinkedResources() {
		return this.linkedResources;
	}
}
