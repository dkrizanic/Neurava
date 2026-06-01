package com.notebook.api.reminders.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminder")
public class Reminder {

	@Id
	private UUID id;

	@Column(name = "owner_account_id", nullable = false)
	private UUID ownerAccountId;

	@Column(name = "workspace_context_id", nullable = false)
	private UUID workspaceContextId;

	@Column(nullable = false, length = 180)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String details;

	@Column(name = "due_at", nullable = false)
	private Instant dueAt;

	@Column(name = "related_context", nullable = false, length = 512)
	private String relatedContext;

	@Column(name = "calendar_sync_enabled", nullable = false)
	private boolean calendarSyncEnabled;

	@Enumerated(EnumType.STRING)
	@Column(name = "calendar_sync_state", nullable = false, length = 40)
	private CalendarSyncState calendarSyncState;

	@Column(name = "calendar_event_id", length = 160)
	private String calendarEventId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	protected Reminder() {
	}

	private Reminder(UUID ownerAccountId, UUID workspaceContextId, String title, String details, Instant dueAt,
			String relatedContext, boolean calendarSyncEnabled, boolean calendarEnabled, Instant now) {
		this.id = UUID.randomUUID();
		this.ownerAccountId = ownerAccountId;
		this.workspaceContextId = workspaceContextId;
		this.createdAt = now;
		update(title, details, dueAt, relatedContext, calendarSyncEnabled, calendarEnabled, now);
	}

	public static Reminder create(UUID ownerAccountId, UUID workspaceContextId, String title, String details,
			Instant dueAt, String relatedContext, boolean calendarSyncEnabled, boolean calendarEnabled, Instant now) {
		return new Reminder(ownerAccountId, workspaceContextId, title, details, dueAt, relatedContext,
				calendarSyncEnabled, calendarEnabled, now);
	}

	public void update(String title, String details, Instant dueAt, String relatedContext, boolean calendarSyncEnabled,
			boolean calendarEnabled, Instant now) {
		this.title = title.trim();
		this.details = details == null ? "" : details;
		this.dueAt = dueAt;
		this.relatedContext = relatedContext == null ? "" : relatedContext.trim();
		this.calendarSyncEnabled = calendarSyncEnabled;
		this.calendarSyncState = syncState(calendarSyncEnabled, calendarEnabled);
		this.calendarEventId = this.calendarSyncState == CalendarSyncState.SYNCED
				? "notebook-reminder-%s".formatted(this.id)
				: null;
		this.updatedAt = now;
	}

	public void complete(Instant now) {
		this.completedAt = now;
		this.updatedAt = now;
	}

	public void reopen(Instant now) {
		this.completedAt = null;
		this.updatedAt = now;
	}

	private static CalendarSyncState syncState(boolean syncRequested, boolean calendarEnabled) {
		if (!syncRequested) {
			return CalendarSyncState.NOT_SYNCED;
		}
		return calendarEnabled ? CalendarSyncState.SYNCED : CalendarSyncState.FAILED;
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

	public String getDetails() {
		return this.details;
	}

	public Instant getDueAt() {
		return this.dueAt;
	}

	public String getRelatedContext() {
		return this.relatedContext;
	}

	public boolean isCalendarSyncEnabled() {
		return this.calendarSyncEnabled;
	}

	public CalendarSyncState getCalendarSyncState() {
		return this.calendarSyncState;
	}

	public String getCalendarEventId() {
		return this.calendarEventId;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	public Instant getCompletedAt() {
		return this.completedAt;
	}
}

